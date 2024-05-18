package com.swd.uniportal.application.admission.training_program;

import com.swd.uniportal.application.admission.exception.AdmissionTrainingProgramIsBeingUsed;
import com.swd.uniportal.application.admission.exception.AdmissionTrainingProgramNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorRepository;
import com.swd.uniportal.infrastructure.repository.AdmissionTrainingProgramRepository;
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
public final class DeleteAdmissionTrainingProgram {

    @RestController
    @Tag(name = "admission-plans")
    public static final class DeleteAdmissionTrainingProgramController extends BaseController {

        private final DeleteAdmissionTrainingProgramService service;

        @Autowired
        public DeleteAdmissionTrainingProgramController(DeleteAdmissionTrainingProgramService service) {
            this.service = service;
        }

        @DeleteMapping("/admission-plans/training-programs/{trainingProgramId}")
        @Operation(summary = "Delete admission training program.")
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
                @PathVariable("trainingProgramId") Long trainingProgramId
        ) {
            if (trainingProgramId < 1) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of("Training program id must be positive.")));
            }
            try {
                service.delete(trainingProgramId);
                return ResponseEntity.noContent().build();
            } catch (AdmissionTrainingProgramNotFoundException
                    | AdmissionTrainingProgramIsBeingUsed e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class DeleteAdmissionTrainingProgramService {

        private final DeleteAdmissionTrainingProgramDatasource datasource;

        @Autowired
        public DeleteAdmissionTrainingProgramService(DeleteAdmissionTrainingProgramDatasource datasource) {
            this.datasource = datasource;
        }

        public void delete(Long trainingProgramId)
                throws AdmissionTrainingProgramNotFoundException,
                AdmissionTrainingProgramIsBeingUsed {
            if (datasource.admissionTrainingProgramDoesNotExist(trainingProgramId)) {
                throw new AdmissionTrainingProgramNotFoundException(String
                        .format("Admission training program with id '%d' not found.", trainingProgramId));
            }
            if (datasource.admissionTrainingProgramIsBeingUsed(trainingProgramId)) {
                throw new AdmissionTrainingProgramIsBeingUsed(String
                        .format("Admission training program with id '%d' is being used.", trainingProgramId));
            }
            datasource.deleteAdmissionTrainingProgram(trainingProgramId);
        }
    }

    @Datasource
    public static final class DeleteAdmissionTrainingProgramDatasource {

        private final AdmissionTrainingProgramRepository admissionTrainingProgramRepository;
        private final AdmissionMajorRepository admissionMajorRepository;

        @Autowired
        public DeleteAdmissionTrainingProgramDatasource(
                AdmissionTrainingProgramRepository admissionTrainingProgramRepository,
                AdmissionMajorRepository admissionMajorRepository
        ) {
            this.admissionTrainingProgramRepository = admissionTrainingProgramRepository;
            this.admissionMajorRepository = admissionMajorRepository;
        }

        public boolean admissionTrainingProgramDoesNotExist(Long trainingProgramId) {
            return !admissionTrainingProgramRepository.existsById(trainingProgramId);
        }

        public void deleteAdmissionTrainingProgram(Long trainingProgramId) {
            admissionTrainingProgramRepository.deleteById(trainingProgramId);
        }

        public boolean admissionTrainingProgramIsBeingUsed(Long trainingProgramId) {
            return admissionMajorRepository.existsByAdmissionTrainingProgramId(trainingProgramId);
        }
    }
}
