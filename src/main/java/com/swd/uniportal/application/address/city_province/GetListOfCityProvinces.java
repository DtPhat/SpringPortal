package com.swd.uniportal.application.address.city_province;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.domain.address.QCityProvince;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetListOfCityProvinces {

    @Builder
    public record GetCityProvincesRequest(String search, SortOrder sortOrder, Long page, boolean all) {
    }

    @Builder
    public record CityProvincesResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                        List<CityProvinceResponse> cityProvinces) {
    }

    @Builder
    public record CityProvinceResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    public static final class GetListOfCityProvincesController extends BaseController {

        private final GetListOfCityProvincesService service;

        public GetListOfCityProvincesController(GetListOfCityProvincesService service) {
            this.service = service;
        }

        @GetMapping("/addresses/wards/districts/city-provinces")
        @Operation(summary = "Get list of city provinces.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CityProvincesResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid parameters.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FailedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "500",
                description = "Server error.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FailedResponse.class)
                )
        )
        public ResponseEntity<Object> get(
                @RequestParam(name = "search", required = false) String search,
                @RequestParam(name = "sort", defaultValue = "ASC") SortOrder sortOrder,
                @RequestParam(name = "page", defaultValue = "1") Long page,
                @RequestParam(name = "all", defaultValue = "false") boolean all) {
            try {
                if (page < 1) {
                    return ResponseEntity.badRequest().body(new FailedResponse(List
                            .of("Page must be positive (page > 0)")));
                }
                CityProvincesResponse response = service.get(GetCityProvincesRequest.builder()
                        .search(StringUtils.trimToEmpty(search))
                        .sortOrder(sortOrder)
                        .page(page)
                        .all(all)
                        .build());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.", e.getMessage())));
            }
        }

    }

    @Service
    public static final class GetListOfCityProvincesService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfCityProvincesDatasource datasource;

        public GetListOfCityProvincesService(GetListOfCityProvincesDatasource datasource) {
            this.datasource = datasource;
        }

        public CityProvincesResponse get(GetCityProvincesRequest request) {
            List<CityProvince> cityProvinces;
            if (request.all()) {
                cityProvinces = datasource.getAll(request.search());
            } else {
                cityProvinces = datasource.getCityProvinces(request);
            }
            long count = datasource.getCountOfCityProvinces(request);
            long totalPages = (long) Math.ceil((double) count / pageSize);
            return CityProvincesResponse.builder()
                    .page(request.page())
                    .totalPages(request.all() ? 1 : totalPages)
                    .pageSize(request.all() ? count : pageSize)
                    .currentPageSize((long) cityProvinces.size())
                    .cityProvinces(cityProvinces.stream().map(cityProvince -> CityProvinceResponse.builder()
                            .id(cityProvince.getId())
                            .name(cityProvince.getName())
                            .build()).toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetListOfCityProvincesDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final CityProvinceRepository cityProvinceRepository;
        private final EntityManager entityManager;

        public GetListOfCityProvincesDatasource(CityProvinceRepository cityProvinceRepository, EntityManager entityManager) {
            this.cityProvinceRepository = cityProvinceRepository;
            this.entityManager = entityManager;
        }

        public List<CityProvince> getCityProvinces(GetCityProvincesRequest request) {
            QCityProvince cityProvince = QCityProvince.cityProvince;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(cityProvince.name.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(cityProvince)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? cityProvince.name.desc() : cityProvince.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfCityProvinces(GetCityProvincesRequest request) {
            QCityProvince cityProvince = QCityProvince.cityProvince;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(cityProvince.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(cityProvince)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? cityProvince.name.desc() : cityProvince.name.asc())
                    .stream().count();
        }

        public List<CityProvince> getAll(String search) {
            return cityProvinceRepository.getAllBySearch(search);
        }
    }
}
