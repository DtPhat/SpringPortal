package com.swd.uniportal.application.address.ward;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.QWard;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.WardRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetListOfWards {

    @Builder
    public record GetWardsRequest(String search, SortOrder sortOrder, Long page, boolean all) {
    }

    @Builder
    public record WardsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                List<WardResponse> wards) {
    }

    @Builder
    public record WardResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    public static final class GetListOfWardsController extends BaseController {

        private final GetListOfWardsService service;

        public GetListOfWardsController(GetListOfWardsService service) {
            this.service = service;
        }

        @GetMapping("/addresses/wards")
        @Operation(summary = "Get list of wards.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = WardsResponse.class)
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
                WardsResponse response = service.get(GetWardsRequest.builder()
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
    public static final class GetListOfWardsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfWardsDatasource datasource;

        public GetListOfWardsService(GetListOfWardsDatasource datasource) {
            this.datasource = datasource;
        }

        public WardsResponse get(GetWardsRequest request) {
            List<Ward> wards;
            if (request.all()) {
                Sort sort = Sort.by(request.sortOrder == SortOrder.ASC
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                        "name");
                wards = datasource.getAll(request, sort);
            } else {
                wards = datasource.getWards(request);
            }
            long count = datasource.getCountOfWards(request);
            long totalPages = (long) Math.ceil((double) count / pageSize);
            return WardsResponse.builder()
                    .page(request.page())
                    .totalPages(request.all() ? 1 : totalPages)
                    .pageSize(request.all() ? wards.size() : pageSize)
                    .currentPageSize((long) wards.size())
                    .wards(wards.stream().map(ward -> WardResponse.builder()
                            .id(ward.getId())
                            .name(ward.getName())
                            .build()).toList())
                    .build();
        }

    }

    @Datasource
    public static final class GetListOfWardsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final WardRepository wardRepository;
        private final EntityManager entityManager;

        public GetListOfWardsDatasource(WardRepository wardRepository, EntityManager entityManager) {
            this.wardRepository = wardRepository;
            this.entityManager = entityManager;
        }

        public List<Ward> getWards(GetWardsRequest request) {
            QWard ward = QWard.ward;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(ward.name.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(ward)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? ward.name.desc() : ward.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfWards(GetWardsRequest request) {
            QWard ward = QWard.ward;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(ward.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(ward)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? ward.name.desc() : ward.name.asc())
                    .stream().count();
        }

        public List<Ward> getAll(GetWardsRequest request, Sort sort) {
            return wardRepository.getAllBySearchSorted(request.search, sort);
        }
    }

}
