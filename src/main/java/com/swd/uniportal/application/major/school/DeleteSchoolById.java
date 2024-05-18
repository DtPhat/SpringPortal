package com.swd.uniportal.application.major.school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.School;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DepartmentRepository;
import com.swd.uniportal.infrastructure.repository.MajorRepository;
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
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteSchoolById {

    @Builder
    public record DeletedSchoolResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class DeleteSchoolController extends BaseController {

        private final DeleteSchoolService service;

        @DeleteMapping("/majors/departments/schools/{id}")
        @Operation(summary = "Delete a school by ID.")
        @ApiResponse(responseCode = "200", description = "Successfully deleted.")
        @ApiResponse(responseCode = "404", description = "School not found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        @ApiResponse(responseCode = "500", description = "Server error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        public ResponseEntity<Object> deleteSchoolById(@PathVariable("id") Long id) {
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
    public static class DeleteSchoolService {

        private final DeleteSchoolDatasource datasource;

        public DeletedSchoolResponse deleteById(Long id) throws Exception {
            Optional<School> schoolOptional = datasource.findById(id);
            if (schoolOptional.isEmpty()) {
                throw new Exception("School not found");
            }
            School school = schoolOptional.get();

            datasource.deleteSchool(school);

            return new DeletedSchoolResponse(school.getId(), school.getName(), school.getCode(), school.getDescription());
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteSchoolDatasource {

        private final SchoolRepository schoolRepository;
        private final DepartmentRepository departmentRepository;
        private final MajorRepository majorRepository;

        public void deleteSchool(School school) {
            schoolRepository.delete(school);
        }

        public Optional<School> findById(Long id) {
            return schoolRepository.findByIdWithDepartments(id);
        }
    }
}
