package com.swd.uniportal.application.admission.training_program;

import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.exception.AdmissionPlanNotFoundException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddAdmissionTrainingProgram {

    @RestController
    @Tag(name = "admission-plans")
    public static final class AddAdmissionTrainingProgramController extends BaseController {

        private final AddAdmissionTrainingProgramService service;

        @Autowired
        public AddAdmissionTrainingProgramController(AddAdmissionTrainingProgramService service) {
            this.service = service;
        }

        @PostMapping("/admission-plans/{admissionId}/training-programs")
        @Operation(summary = "Add admission training program.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission training program to add.",
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
        @SuppressWarnings("Duplicates")
        public ResponseEntity<Object> add(
                @PathVariable("admissionId") Long admissionId,
                @RequestBody ModifyAdmissionTrainingProgramDto request
        ) {
            if (admissionId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission plan is must be positive.")));
            }
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            request.setName(StringUtils.trim(request.getName()));
            try {
                AdmissionTrainingProgramDto added = service.add(admissionId, request);
                return ResponseEntity.ok(added);
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
    public static final class AddAdmissionTrainingProgramService {

        private final AddAdmissionTrainingProgramDatasource datasource;
        private final Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> mapper;

        @Autowired
        public AddAdmissionTrainingProgramService(
                AddAdmissionTrainingProgramDatasource datasource,
                Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        @SuppressWarnings("Duplicates")
        public AdmissionTrainingProgramDto add(Long admissionId, ModifyAdmissionTrainingProgramDto request)
                throws AdmissionPlanNotFoundException,
                TrainingProgramNotFoundException,
                DuplicateAdmissionTrainingProgramNameException {
            TrainingProgram trainingProgram = datasource.getTrainingProgram(request.getTrainingProgramId())
                    .orElseThrow(() -> new TrainingProgramNotFoundException(String
                            .format("Training program with id '%d' not found.", request.getTrainingProgramId())));
            AdmissionPlan admissionPlan = datasource.getAdmissionPlanById(admissionId)
                    .orElseThrow(() -> new AdmissionPlanNotFoundException(String
                            .format("Admission plan with id '%d' not found.", admissionId)));
            if (admissionPlan.getAdmissionTrainingPrograms().stream()
                    .anyMatch(atp -> StringUtils.equals(atp.getName(), request.getName()))) {
                throw new DuplicateAdmissionTrainingProgramNameException(String
                        .format("Admission training name '%s' already exists.", request.getName()));
            }
            AdmissionTrainingProgram admissionTrainingProgram = AdmissionTrainingProgram.builder()
                    .name(request.getName())
                    .trainingProgram(trainingProgram)
                    .admissionPlan(admissionPlan)
                    .build();
            admissionTrainingProgram = datasource.update(admissionTrainingProgram);
            return mapper.toDto(admissionTrainingProgram);
        }
    }

    @Datasource
    public static final class AddAdmissionTrainingProgramDatasource {

        private final AdmissionPlanRepository admissionPlanRepository;
        private final TrainingProgramRepository trainingProgramRepository;
        private final AdmissionTrainingProgramRepository admissionTrainingProgramRepository;

        @Autowired
        public AddAdmissionTrainingProgramDatasource(
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
    }
}
