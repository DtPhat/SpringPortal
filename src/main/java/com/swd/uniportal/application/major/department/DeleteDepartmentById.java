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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteDepartmentById {

    @Builder
    public record DeletedDepartmentResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class DeleteDepartmentController extends BaseController {

        private final DeleteDepartmentService service;

        @DeleteMapping("/majors/departments/{id}")
        @Operation(summary = "Delete a department by ID.")
        @ApiResponse(responseCode = "200", description = "Successfully deleted.")
        @ApiResponse(responseCode = "404", description = "Department not found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        @ApiResponse(responseCode = "500", description = "Server error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        public ResponseEntity<Object> deleteDepartmentById(@PathVariable("id") Long id) {
            try {
                return ResponseEntity.ok(service.deleteById(id));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class DeleteDepartmentService {

        private final DeleteDepartmentDatasource datasource;

        public DeletedDepartmentResponse deleteById(Long id) throws Exception {
            Optional<Department> departmentOptional = datasource.findById(id);
            if (departmentOptional.isEmpty()) {
                throw new Exception("Department not found");
            }
            Department department = departmentOptional.get();

            datasource.deleteDepartment(department);

            return new DeletedDepartmentResponse(department.getId(), department.getName(), department.getCode(), department.getDescription());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteDepartmentDatasource {

        private final DepartmentRepository departmentRepository;

        public void deleteDepartment(Department department) {
            departmentRepository.delete(department);
        }

        public Optional<Department> findById(Long id) {
            return departmentRepository.findByIdWithMajors(id);
        }
    }
}
