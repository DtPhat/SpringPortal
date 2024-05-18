package com.swd.uniportal.application.major.department;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.Department;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetDepartmentById {

    @Builder
    public record DepartmentResponse(Long id, String name, String code, String description, List<MajorResponse> majors) {
    }

    @Builder
    public record MajorResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class GetDepartmentByIdController extends BaseController {

        private final GetDepartmentByIdService service;

        @GetMapping("/majors/departments/{id}")
        @Operation(summary = "Get a department based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DepartmentResponse.class)
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
                description = "No permission to get department with current role.",
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
        public ResponseEntity<Object> get(@PathVariable("id") Long id) {
            try {
                return ResponseEntity.ok(service.get(id));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class GetDepartmentByIdService {

        private final GetDepartmentByIdDatasource datasource;

        public DepartmentResponse get(Long id) throws Exception {
            Optional<Department> departmentOptional = datasource.findByIdWithMajors(id);
            if (departmentOptional.isEmpty()) {
                throw new Exception("Department not found");
            }
            Department department = departmentOptional.get();
            List<MajorResponse> majors = department.getMajors().stream()
                    .map(major -> new MajorResponse(
                            major.getId(),
                            major.getName(),
                            major.getCode(),
                            major.getDescription()))
                    .collect(Collectors.toList());

            return new DepartmentResponse(
                    department.getId(),
                    department.getName(),
                    department.getCode(),
                    department.getDescription(),
                    majors);
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetDepartmentByIdDatasource {

        private final DepartmentRepository departmentRepository;

        public Optional<Department> findByIdWithMajors(Long id) {
            return departmentRepository.findByIdWithMajors(id);
        }
    }
}