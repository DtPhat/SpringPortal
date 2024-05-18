package com.swd.uniportal.application.address.district;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.District;
import com.swd.uniportal.domain.address.QDistrict;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DistrictRepository;
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
public class GetListOfDistricts {

    @Builder
    public record GetDistrictsRequest(String search, SortOrder sortOrder, Long page) {
    }

    @Builder
    public record DistrictsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                    List<DistrictResponse> districts) {
    }

    @Builder
    public record DistrictResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    public static final class GetListOfDistrictsController extends BaseController {

        private final GetListOfDistrictsService service;

        public GetListOfDistrictsController(GetListOfDistrictsService service) {
            this.service = service;
        }

        @GetMapping("/addresses/wards/districts")
        @Operation(summary = "Get list of districts.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DistrictsResponse.class)
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
                @RequestParam(name = "page", defaultValue = "1") Long page) {
            try {
                if (page < 1) {
                    return ResponseEntity.badRequest().body(new FailedResponse(List
                            .of("Page must be positive (page > 0)")));
                }
                DistrictsResponse response = service.get(GetDistrictsRequest.builder()
                        .search(StringUtils.trimToNull(search))
                        .sortOrder(sortOrder)
                        .page(page)
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
    public static final class GetListOfDistrictsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfDistrictsDatasource datasource;

        public GetListOfDistrictsService(GetListOfDistrictsDatasource datasource) {
            this.datasource = datasource;
        }

        public DistrictsResponse get(GetDistrictsRequest request) {
            List<District> districts = datasource.getDistricts(request);
            Long totalPages = (long) Math.ceil((double) datasource.getCountOfDistricts(request) / pageSize);
            return DistrictsResponse.builder()
                    .page(request.page())
                    .totalPages(totalPages)
                    .pageSize(pageSize)
                    .currentPageSize((long) districts.size())
                    .districts(districts.stream().map(district -> DistrictResponse.builder()
                            .id(district.getId())
                            .name(district.getName())
                            .build()).toList())
                    .build();
        }

    }

    @Datasource
    public static final class GetListOfDistrictsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final DistrictRepository districtRepository;
        private final EntityManager entityManager;

        public GetListOfDistrictsDatasource(DistrictRepository districtRepository, EntityManager entityManager) {
            this.districtRepository = districtRepository;
            this.entityManager = entityManager;
        }

        public List<District> getDistricts(GetDistrictsRequest request) {
            QDistrict district = QDistrict.district;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(district.name.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(district)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? district.name.desc() : district.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfDistricts(GetDistrictsRequest request) {
            QDistrict district = QDistrict.district;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(district.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(district)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? district.name.desc() : district.name.asc())
                    .stream().count();
        }

    }

}
