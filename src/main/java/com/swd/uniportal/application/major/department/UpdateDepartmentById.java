package com.swd.uniportal.application.major.department;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.Department;
import com.swd.uniportal.domain.major.School;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DepartmentRepository;
import com.swd.uniportal.infrastructure.repository.SchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateDepartmentById {

    @Data
    public static class UpdateDepartmentRequest {

        private String name;

        private String code;

        private String description;

        private Long schoolId;
    }

    @Builder
    public record DepartmentUpdatedResponse(Long id, String name, String code, String description, Long schoolId) {
    }

    @RestController
    @AllArgsConstructor
    @Tag(name = "majors")
    public static class UpdateDepartmentController extends BaseController {

        private final UpdateDepartmentService service;

        @PutMapping("/majors/departments/{id}")
        @Operation(summary = "Update a department by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Department info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateDepartmentRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DepartmentUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or Department not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateDepartmentRequest request) {
            try {
                DepartmentUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }

    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateDepartmentService {

        private final UpdateDepartmentDatasource datasource;

        public DepartmentUpdatedResponse update(Long id, UpdateDepartmentRequest request) throws Exception {
            Optional<Department> optionalDepartment = datasource.findById(id);
            if (optionalDepartment.isEmpty()) {
                throw new Exception("Department not found");
            }
            Department department = optionalDepartment.get();

            if (StringUtils.isNotBlank(request.getName())) {
                department.setName(StringUtils.trim(request.getName()));
            }
            if (StringUtils.isNotBlank(request.getCode())) {
                department.setCode(StringUtils.trim(request.getCode()));
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                department.setDescription(StringUtils.trim(request.getDescription()));
            }
            if (request.getSchoolId() != null) {
                Optional<School> optionalSchool = datasource.findSchoolById(request.getSchoolId());
                if (optionalSchool.isEmpty()) {
                    throw new IllegalArgumentException("School with the provided ID not found");
                }
                department.setSchool(optionalSchool.get());
            }

            department = datasource.update(department);

            return new DepartmentUpdatedResponse(
                    department.getId(), department.getName(), department.getCode(), department.getDescription(), department.getSchool().getId());
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateDepartmentDatasource {

        private final DepartmentRepository departmentRepository;
        private final SchoolRepository schoolRepository;

        public Optional<Department> findById(Long id) {
            return departmentRepository.findById(id);
        }

        public Optional<School> findSchoolById(Long id) {
            return schoolRepository.findById(id);
        }

        public Department update(Department department) {
            return departmentRepository.save(department);
        }
    }
}
