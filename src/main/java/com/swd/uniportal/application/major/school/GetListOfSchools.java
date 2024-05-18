package com.swd.uniportal.application.major.school;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.QSchool;
import com.swd.uniportal.domain.major.School;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.SchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
public class GetListOfSchools {

    @Builder
    public record GetSchoolsRequest(String search, SortOrder sortOrder, Long page) {
    }

    @Builder
    public record SchoolsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize, List<SchoolResponse> schools) {
    }

    @Builder
    public record SchoolResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class GetSchoolsController extends BaseController {

        private final GetSchoolsService service;

        @GetMapping("/majors/departments/schools")
        @Operation(summary = "Get list of schools.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SchoolsResponse.class)
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
                responseCode = "403",
                description = "No permission to get school list with current role.",
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
                SchoolsResponse response =  service.get(GetSchoolsRequest.builder()
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
    public static class GetSchoolsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetSchoolsDatasource datasource;

        public GetSchoolsService(GetSchoolsDatasource datasource) {
            this.datasource = datasource;
        }

        public SchoolsResponse get(GetSchoolsRequest request) {
            List<School> schools = datasource.getSchools(request);
            Long totalPages = (long) Math.ceil((double) datasource.getCountOfSchools(request) / pageSize);
            return SchoolsResponse.builder()
                    .page(request.page())
                    .currentPageSize((long) schools.size())
                    .pageSize(pageSize)
                    .totalPages(totalPages)
                    .schools(schools.stream().map(school -> SchoolResponse.builder()
                            .id(school.getId())
                            .name(school.getName())
                            .code(school.getCode())
                            .description(school.getDescription())
                            .build()).toList())
                    .build();
        }

    }

    @Datasource
    public static class GetSchoolsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final SchoolRepository schoolRepository;
        private final EntityManager entityManager;

        public GetSchoolsDatasource(SchoolRepository schoolRepository, EntityManager entityManager) {
            this.schoolRepository = schoolRepository;
            this.entityManager = entityManager;
        }

        public List<School> getSchools(GetSchoolsRequest request) {
            QSchool school = QSchool.school;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(school.name.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(school)
                    .where(filters)
                    .orderBy((request.sortOrder == SortOrder.DESC) ? school.name.desc() : school.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public Long getCountOfSchools(GetSchoolsRequest request) {
            QSchool school = QSchool.school;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(school.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(school)
                    .where(filters)
                    .orderBy((request.sortOrder == SortOrder.DESC) ? school.name.desc() : school.name.asc())
                    .stream().count();
        }

    }

}
