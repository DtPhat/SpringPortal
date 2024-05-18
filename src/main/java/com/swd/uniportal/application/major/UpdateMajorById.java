package com.swd.uniportal.application.major;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.Department;
import com.swd.uniportal.domain.major.Major;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DepartmentRepository;
import com.swd.uniportal.infrastructure.repository.MajorRepository;
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
public class UpdateMajorById {

    @Data
    public static class UpdateMajorRequest {

        private String name;

        private String code;

        private String description;

        private Long departmentId;
    }

    @Builder
    public record MajorUpdatedResponse(Long id, String name, String code, String description, Long departmentId) {
    }

    @RestController
    @AllArgsConstructor
    @Tag(name = "majors")
    public static class UpdateMajorController extends BaseController {

        private final UpdateMajorService service;

        @PutMapping("/majors/{id}")
        @Operation(summary = "Update a major by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Major info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateMajorRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MajorUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or Major not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateMajorRequest request) {
            try {
                MajorUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }

    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateMajorService {

        private final UpdateMajorDatasource datasource;

        public MajorUpdatedResponse update(Long id, UpdateMajorRequest request) throws Exception {
            Optional<Major> optionalMajor = datasource.findById(id);
            if (optionalMajor.isEmpty()) {
                throw new Exception("Major not found");
            }
            Major major = optionalMajor.get();

            if (StringUtils.isNotBlank(request.getName())) {
                major.setName(StringUtils.trim(request.getName()));
            }
            if (StringUtils.isNotBlank(request.getCode())) {
                major.setCode(StringUtils.trim(request.getCode()));
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                major.setDescription(StringUtils.trim(request.getDescription()));
            }
            if (request.getDepartmentId() != null) {
                Optional<Department> optionalDepartment = datasource.findDepartmentById(request.getDepartmentId());
                if (optionalDepartment.isEmpty()) {
                    throw new IllegalArgumentException("Department with the provided ID not found");
                }
                major.setDepartment(optionalDepartment.get());
            }

            major = datasource.update(major);

            return new MajorUpdatedResponse(
                    major.getId(), major.getName(), major.getCode(), major.getDescription(), major.getDepartment().getId());
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateMajorDatasource {

        private final MajorRepository majorRepository;
        private final DepartmentRepository departmentRepository;

        public Optional<Major> findById(Long id) {
            return majorRepository.findById(id);
        }

        public Optional<Department> findDepartmentById(Long id) {
            return departmentRepository.findById(id);
        }

        public Major update(Major major) {
            return majorRepository.save(major);
        }
    }
}
