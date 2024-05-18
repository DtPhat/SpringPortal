package com.swd.uniportal.application.admission.training_program;

import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.exception.AdmissionPlanNotFoundException;
import com.swd.uniportal.application.admission.exception.AdmissionTrainingProgramNotFoundException;
import com.swd.uniportal.application.admission.exception.DuplicateAdmissionTrainingProgramNameException;
import com.swd.uniportal.application.admission.exception.TrainingProgramNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionPlan;
import com.swd.uniportal.domain.admission.AdmissionTrainingProgram;
import com.swd.uniportal.domain.admission.TrainingProgram;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionPlanRepository;
import com.swd.uniportal.infrastructure.repository.AdmissionTrainingProgramRepository;
import com.swd.uniportal.infrastructure.repository.TrainingProgramRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateAdmissionTrainingProgram {

    @RestController
    @Tag(name = "admission-plans")
    public static final class UpdateAdmissionTrainingProgramController extends BaseController {

        private final UpdateAdmissionTrainingProgramService service;

        @Autowired
        public UpdateAdmissionTrainingProgramController(UpdateAdmissionTrainingProgramService service) {
            this.service = service;
        }

        @PutMapping("/admission-plans/{admissionId}/training-programs/{trainingProgramId}")
        @Operation(summary = "Update admission training program.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission training program to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ModifyAdmissionTrainingProgramDto.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionPlanDto.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body.",
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
        public ResponseEntity<Object> update(
                @PathVariable("admissionId") Long admissionId,
                @PathVariable("trainingProgramId") Long trainingProgramId,
                @RequestBody ModifyAdmissionTrainingProgramDto request
        ) {
            if (admissionId < 1) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of("Admission id must be positive.")));
            }
            if (trainingProgramId < 1) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of("Training program id must be positive.")));
            }
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            request.setName(StringUtils.trim(request.getName()));
            try {
                AdmissionTrainingProgramDto updated = service.update(admissionId, trainingProgramId, request);
                return ResponseEntity.ok(updated);
            } catch (AdmissionPlanNotFoundException
                     | TrainingProgramNotFoundException
                     | DuplicateAdmissionTrainingProgramNameException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static final class UpdateAdmissionTrainingProgramService {

        private final UpdateAdmissionTrainingProgramDatasource datasource;
        private final Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> mapper;

        @Autowired
        public UpdateAdmissionTrainingProgramService(
                UpdateAdmissionTrainingProgramDatasource datasource,
                Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        @SuppressWarnings("Duplicates")
        public AdmissionTrainingProgramDto update(Long admissionId, Long trainingProgramId, ModifyAdmissionTrainingProgramDto request)
                throws AdmissionPlanNotFoundException,
                TrainingProgramNotFoundException,
                DuplicateAdmissionTrainingProgramNameException,
                AdmissionTrainingProgramNotFoundException {
            AdmissionTrainingProgram admissionTrainingProgram = datasource
                    .getAdmissionTrainingProgram(trainingProgramId)
                    .orElseThrow(() -> new AdmissionTrainingProgramNotFoundException(String
                            .format("Admission training program with id '%d' not found.", trainingProgramId)));
            TrainingProgram trainingProgram = datasource.getTrainingProgram(request.getTrainingProgramId())
                    .orElseThrow(() -> new TrainingProgramNotFoundException(String
                            .format("Training program with id '%d' not found.", request.getTrainingProgramId())));
            AdmissionPlan admissionPlan = datasource.getAdmissionPlanById(admissionId)
                    .orElseThrow(() -> new AdmissionPlanNotFoundException(String
                            .format("Admission plan with id '%d' not found.", admissionId)));
            if (!StringUtils.equals(admissionTrainingProgram.getName(), request.getName()) && admissionPlan.getAdmissionTrainingPrograms().stream()
                    .anyMatch(atp -> StringUtils.equals(atp.getName(), request.getName()))) {
                throw new DuplicateAdmissionTrainingProgramNameException(String
                        .format("Admission training name '%s' already exists.", request.getName()));
            }
            admissionTrainingProgram.setName(request.getName());
            admissionTrainingProgram.setTrainingProgram(trainingProgram);
            admissionTrainingProgram = datasource.update(admissionTrainingProgram);
            return mapper.toDto(admissionTrainingProgram);
        }
    }

    @Datasource
    public static final class UpdateAdmissionTrainingProgramDatasource {

        private final AdmissionPlanRepository admissionPlanRepository;
        private final TrainingProgramRepository trainingProgramRepository;
        private final AdmissionTrainingProgramRepository admissionTrainingProgramRepository;

        public UpdateAdmissionTrainingProgramDatasource(
                AdmissionPlanRepository admissionPlanRepository,
                TrainingProgramRepository trainingProgramRepository,
                AdmissionTrainingProgramRepository admissionTrainingProgramRepository) {
            this.admissionPlanRepository = admissionPlanRepository;
            this.trainingProgramRepository = trainingProgramRepository;
            this.admissionTrainingProgramRepository = admissionTrainingProgramRepository;
        }

        public Optional<AdmissionPlan> getAdmissionPlanById(Long admissionId) {
            return admissionPlanRepository.getByIdTrainingProgramsPopulated(admissionId);
        }

        public Optional<TrainingProgram> getTrainingProgram(Long trainingProgramId) {
            return trainingProgramRepository.findById(trainingProgramId);
        }

        public AdmissionTrainingProgram update(AdmissionTrainingProgram admissionTrainingProgram) {
            return admissionTrainingProgramRepository.save(admissionTrainingProgram);
        }

        public Optional<AdmissionTrainingProgram> getAdmissionTrainingProgram(Long admissionId) {
            return admissionTrainingProgramRepository.findById(admissionId);
        }
    }
}
