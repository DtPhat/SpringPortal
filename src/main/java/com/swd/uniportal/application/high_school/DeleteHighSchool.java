package com.swd.uniportal.application.high_school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.high_school.exception.HighSchoolNotFoundException;
import com.swd.uniportal.application.high_school.exception.RemovingReferencedHighSchoolException;
import com.swd.uniportal.domain.institution.HighSchool;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.HighSchoolRepository;
import com.swd.uniportal.infrastructure.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteHighSchool {
    @Builder
    public record DeletedHighSchoolResponse(Long id, String name, String description) {
    }

    @RestController
    @Tag(name = "high-schools")
    @AllArgsConstructor
    public static final class DeleteHighSchoolController extends BaseController {

        private final DeleteHighSchoolService service;

        @DeleteMapping("/high-schools/{id}")
        @Operation(summary = "Delete a high school by ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Successfully deleted.")
        @ApiResponse(
                responseCode = "404",
                description = "High school not found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = FailedResponse.class
                        )
                )
        )
        @ApiResponse(
                responseCode = "500",
                description = "Server error.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = FailedResponse.class
                        )
                )
        )
        public ResponseEntity<Object> deleteHighSchoolById(@PathVariable("id") Long id) {
            try {
                return ResponseEntity.ok(service.deleteById(id));
            } catch (HighSchoolNotFoundException | RemovingReferencedHighSchoolException e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class DeleteHighSchoolService {

        private final DeleteHighSchoolDatasource datasource;

        public DeletedHighSchoolResponse deleteById(Long id)
                throws HighSchoolNotFoundException,
                RemovingReferencedHighSchoolException {
            HighSchool highSchool = datasource.get(id);

            if (datasource.doesHighSchoolHaveStudents(id)) {
                throw new RemovingReferencedHighSchoolException("High school is referenced by some students");
            }

            datasource.delete(id);

            return DeletedHighSchoolResponse.builder()
                    .id(highSchool.getId())
                    .description(highSchool.getDescription())
                    .name(highSchool.getName())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class DeleteHighSchoolDatasource {

        private final HighSchoolRepository highSchoolRepository;
        private final StudentRepository studentRepository;

        public HighSchool get(Long id) throws HighSchoolNotFoundException {
            return highSchoolRepository.findById(id).orElseThrow(() -> new HighSchoolNotFoundException("High School not found"));
        }

        public boolean doesHighSchoolHaveStudents(Long id) {
            return studentRepository.existsByHighSchoolId(id);
        }

        public void delete(Long id) {
            highSchoolRepository.deleteById(id);
        }
    }
}
