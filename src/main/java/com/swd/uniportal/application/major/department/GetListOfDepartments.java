package com.swd.uniportal.application.major.department;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.Department;
import com.swd.uniportal.domain.major.QDepartment;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DepartmentRepository;
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
import lombok.Data;
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
public class GetListOfDepartments {

    @Data
    @Builder
    public static final class GetDepartmentsRequest {

        String search;
        SortOrder sortOrder;
        Long page;
        boolean all;
    }

    @Builder
    public record DepartmentsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                      List<DepartmentResponse> departments) {
    }

    @Builder
    public record DepartmentResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class GetDepartmentsController extends BaseController {

        private final GetDepartmentsService service;

        @GetMapping("/majors/departments")
        @Operation(summary = "Get list of departments.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DepartmentsResponse.class)
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
                description = "No permission to get department list with current role.",
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
                DepartmentsResponse response = service.get(GetDepartmentsRequest.builder()
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
    public static class GetDepartmentsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetDepartmentsDatasource datasource;

        public GetDepartmentsService(GetDepartmentsDatasource datasource) {
            this.datasource = datasource;
        }

        public DepartmentsResponse get(GetDepartmentsRequest request) {
            List<Department> departments;
            if (request.isAll()) {
                Sort sort = Sort.by((request.sortOrder == SortOrder.ASC)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                        "name");
                departments = datasource.getAll(request.search, sort);
            } else {
                departments = datasource.getDepartments(request);
            }
            Long count = datasource.getCountOfDepartments(request);
            long totalPages = (long) Math.ceil((double) count / pageSize);
            return DepartmentsResponse.builder()
                    .page(request.getPage())
                    .currentPageSize((long) departments.size())
                    .pageSize(request.isAll() ? count : pageSize)
                    .totalPages(request.isAll() ? 1 : totalPages)
                    .departments(departments.stream().map(department -> DepartmentResponse.builder()
                            .id(department.getId())
                            .name(department.getName())
                            .code(department.getCode())
                            .description(department.getDescription())
                            .build()).toList())
                    .build();
        }

    }

    @Datasource
    public static class GetDepartmentsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final DepartmentRepository departmentRepository;
        private final EntityManager entityManager;

        public GetDepartmentsDatasource(
                DepartmentRepository departmentRepository,
                EntityManager entityManager) {
            this.departmentRepository = departmentRepository;
            this.entityManager = entityManager;
        }

        @SuppressWarnings("Duplicates")
        public List<Department> getDepartments(GetDepartmentsRequest request) {
            QDepartment department = QDepartment.department;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(department.name.containsIgnoreCase(request.getSearch()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(department)
                    .where(filters)
                    .orderBy((request.getSortOrder() == SortOrder.DESC) ? department.name.desc() : department.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        @SuppressWarnings("Duplicates")
        public Long getCountOfDepartments(GetDepartmentsRequest request) {
            QDepartment department = QDepartment.department;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(department.name.containsIgnoreCase(request.getSearch()));
            }
            return factory.selectFrom(department)
                    .where(filters)
                    .orderBy((request.getSortOrder() == SortOrder.DESC) ? department.name.desc() : department.name.asc())
                    .stream().count();
        }

        public List<Department> getAll(String search, Sort sort) {
            return departmentRepository.getAllBySearchOrdered(search, sort);
        }
    }

}
