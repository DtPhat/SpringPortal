package com.swd.uniportal.application.major.school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.School;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.SchoolRepository;
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
public class GetSchoolById {

    @Builder
    public record SchoolResponse(Long id, String name, String code, String description, List<DepartmentResponse> departments) {
    }

    @Builder
    public record DepartmentResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class GetSchoolByIdController extends BaseController {

        private final GetSchoolByIdService service;

        @GetMapping("/majors/departments/schools/{id}")
        @Operation(summary = "Get a school based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SchoolResponse.class)
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
                description = "No permission to get school with current role.",
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
    public static final class GetSchoolByIdService {

        private final GetSchoolByIdDatasource datasource;

        public SchoolResponse get(Long id) throws Exception {
            Optional<School> schoolOptional = datasource.findByIdWithDepartments(id);
            if (schoolOptional.isEmpty()) {
                throw new Exception("School not found");
            }
            School school = schoolOptional.get();
            List<DepartmentResponse> departments = school.getDepartments().stream()
                    .map(department -> new DepartmentResponse(
                            department.getId(),
                            department.getName(),
                            department.getCode(),
                            department.getDescription()))
                    .collect(Collectors.toList());

            return new SchoolResponse(
                    school.getId(),
                    school.getName(),
                    school.getCode(),
                    school.getDescription(),
                    departments);
        }

    }

    @Datasource
    @AllArgsConstructor
    public static final class GetSchoolByIdDatasource {

        private final SchoolRepository schoolRepository;

        public Optional<School> findByIdWithDepartments(Long id) {
            return schoolRepository.findByIdWithDepartments(id);
        }

    }

}
