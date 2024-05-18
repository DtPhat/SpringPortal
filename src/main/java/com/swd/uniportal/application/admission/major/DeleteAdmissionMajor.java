package com.swd.uniportal.application.admission.major;

import com.swd.uniportal.application.admission.exception.AdmissionMajorNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteAdmissionMajor {

    @RestController
    @Tag(name = "admission-plans")
    public static final class DeleteAdmissionMajorController extends BaseController {

        private final DeleteAdmissionMajorService service;

        @Autowired
        public DeleteAdmissionMajorController(DeleteAdmissionMajorService service) {
            this.service = service;
        }

        @DeleteMapping("/admission-plans/majors/{admissionMajorId}")
        @Operation(summary = "Delete admission major.")
        @ApiResponse(
                responseCode = "204",
                description = "Successful."
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request.",
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
        public ResponseEntity<Object> delete(
                @PathVariable("admissionMajorId") Long admissionMajorId
        ) {
            if (admissionMajorId < 1) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of("Admission major id must be positive.")));
            }
            try {
                service.delete(admissionMajorId);
                return ResponseEntity.noContent().build();
            } catch (AdmissionMajorNotFoundException e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class DeleteAdmissionMajorService {

        private final DeleteAdmissionMajorDatasource datasource;

        @Autowired
        public DeleteAdmissionMajorService(DeleteAdmissionMajorDatasource datasource) {
            this.datasource = datasource;
        }

        public void delete(Long admissionMajorId) throws AdmissionMajorNotFoundException {
            if (datasource.admissionMajorDoesNotExist(admissionMajorId)) {
                throw new AdmissionMajorNotFoundException(String
                        .format("Admission major with id '%d' not found.", admissionMajorId));
            }
            datasource.deleteAdmissionTrainingProgram(admissionMajorId);
        }
    }

    @Datasource
    public static final class DeleteAdmissionMajorDatasource {

        private final AdmissionMajorRepository admissionMajorRepository;

        @Autowired
        public DeleteAdmissionMajorDatasource(AdmissionMajorRepository admissionMajorRepository) {
            this.admissionMajorRepository = admissionMajorRepository;
        }

        public boolean admissionMajorDoesNotExist(Long admissionMajorId) {
            return !admissionMajorRepository.existsById(admissionMajorId);
        }

        public void deleteAdmissionTrainingProgram(Long admissionMajorId) {
            admissionMajorRepository.deleteById(admissionMajorId);
        }
    }
}
