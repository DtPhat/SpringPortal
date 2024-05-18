package com.swd.uniportal.application.major;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.major.dto.DepartmentDto;
import com.swd.uniportal.domain.major.Major;
import com.swd.uniportal.domain.major.QMajor;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
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
public class GetListOfMajors {

    @Builder
    public record GetMajorsRequest(String search, SortOrder sortOrder, Long page, boolean all) {
    }

    @Builder
    public record MajorsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                 List<MajorResponse> majors) {
    }

    @Builder
    public record MajorResponse(Long id, String name, String code, String description, DepartmentDto department) {
    }

    @RestController
    @Tag(name = "majors")
    public static final class GetListOfMajorsController extends BaseController {

        private final GetListOfMajorsService service;

        public GetListOfMajorsController(GetListOfMajorsService service) {
            this.service = service;
        }

        @GetMapping("/majors")
        @Operation(summary = "Get list of majors.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MajorsResponse.class)
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
                MajorsResponse response = service.get(GetMajorsRequest.builder()
                        .search(StringUtils.trimToNull(search))
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
    public static final class GetListOfMajorsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfMajorsDatasource datasource;

        public GetListOfMajorsService(GetListOfMajorsDatasource datasource) {
            this.datasource = datasource;
        }

        public MajorsResponse get(GetMajorsRequest request) {
            List<Major> majors = datasource.getMajors(request);
            long count = datasource.getCountOfMajors(request);
            long totalPages = Math.ceilDiv(count, request.all() ? count : pageSize);
            return MajorsResponse.builder()
                    .page(request.page())
                    .totalPages(totalPages)
                    .pageSize(request.all() ? count : pageSize)
                    .currentPageSize((long) majors.size())
                    .majors(majors.stream()
                            .map(major -> MajorResponse.builder()
                                    .id(major.getId())
                                    .name(major.getName())
                                    .code(major.getCode())
                                    .description(major.getDescription())
                                    .department(DepartmentDto.builder()
                                            .id(major.getDepartment().getId())
                                            .name(major.getDepartment().getName())
                                            .code(major.getDepartment().getCode())
                                            .build())
                                    .build())
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetListOfMajorsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final EntityManager entityManager;

        public GetListOfMajorsDatasource(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public List<Major> getMajors(GetMajorsRequest request) {
            QMajor major = QMajor.major;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(major.name.containsIgnoreCase(request.search()));
            }
            if (Boolean.TRUE.equals(request.all())) {
                return factory.selectFrom(major)
                        .where(filters)
                        .orderBy((request.sortOrder() == SortOrder.DESC) ? major.name.desc() : major.name.asc())
                        .fetch();
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(major)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? major.name.desc() : major.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfMajors(GetMajorsRequest request) {
            QMajor major = QMajor.major;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(major.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(major)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? major.name.desc() : major.name.asc())
                    .stream().count();
        }

    }

}
