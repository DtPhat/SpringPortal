package com.swd.uniportal.application.admission.major;

import com.swd.uniportal.application.admission.dto.AdmissionMajorDto;
import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionMajorDto;
import com.swd.uniportal.application.admission.exception.AdmissionMajorNotFoundException;
import com.swd.uniportal.application.admission.exception.AdmissionMajorNotInCurrentPlanException;
import com.swd.uniportal.application.admission.exception.AdmissionPlanNotFoundException;
import com.swd.uniportal.application.admission.exception.AdmissionTrainingProgramNotFoundException;
import com.swd.uniportal.application.admission.exception.AdmissionTrainingProgramNotInAdmissionPlanException;
import com.swd.uniportal.application.admission.exception.DuplicateAdmissionMajorNameException;
import com.swd.uniportal.application.admission.exception.MajorNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMajor;
import com.swd.uniportal.domain.admission.AdmissionPlan;
import com.swd.uniportal.domain.admission.AdmissionTrainingProgram;
import com.swd.uniportal.domain.major.Major;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorRepository;
import com.swd.uniportal.infrastructure.repository.AdmissionPlanRepository;
import com.swd.uniportal.infrastructure.repository.AdmissionTrainingProgramRepository;
import com.swd.uniportal.infrastructure.repository.MajorRepository;
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
public final class UpdateAdmissionMajor {

    @RestController
    @Tag(name = "admission-plans")
    public static final class UpdateAdmissionMajorController extends BaseController {

        private final UpdateAdmissionMajorService service;

        @Autowired
        public UpdateAdmissionMajorController(UpdateAdmissionMajorService service) {
            this.service = service;
        }

        @PutMapping("/admission-plans/{admissionId}/majors/{admissionMajorId}")
        @Operation(summary = "Update admission major.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission training program to add.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ModifyAdmissionMajorDto.class)
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
                @PathVariable("admissionMajorId") Long admissionMajorId,
                @RequestBody ModifyAdmissionMajorDto request
        ) {
            if (admissionId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission plan id must be positive.")));
            }
            if (admissionMajorId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission major id must be positive.")));
            }
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            request.setName(StringUtils.trim(request.getName()));
            try {
                AdmissionMajorDto updated = service.update(admissionId, admissionMajorId, request);
                return ResponseEntity.ok(updated);
            } catch (AdmissionPlanNotFoundException
                     | MajorNotFoundException
                     | AdmissionTrainingProgramNotFoundException
                     | DuplicateAdmissionMajorNameException
                     | AdmissionTrainingProgramNotInAdmissionPlanException
                     | AdmissionMajorNotFoundException
                     | AdmissionMajorNotInCurrentPlanException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static final class UpdateAdmissionMajorService {

        private final UpdateAdmissionMajorDatasource datasource;
        private final Mapper<AdmissionMajor, AdmissionMajorDto> mapper;

        @Autowired
        public UpdateAdmissionMajorService(
                UpdateAdmissionMajorDatasource datasource,
                Mapper<AdmissionMajor, AdmissionMajorDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        @SuppressWarnings("Duplicates")
        public AdmissionMajorDto update(Long admissionId, Long admissionMajorId, ModifyAdmissionMajorDto request)
                throws AdmissionPlanNotFoundException,
                MajorNotFoundException,
                AdmissionTrainingProgramNotFoundException,
                DuplicateAdmissionMajorNameException,
                AdmissionTrainingProgramNotInAdmissionPlanException,
                AdmissionMajorNotFoundException,
                AdmissionMajorNotInCurrentPlanException {
            AdmissionMajor admissionMajor = datasource.getAdmissionMajor(admissionMajorId)
                    .orElseThrow(() -> new AdmissionMajorNotFoundException(String
                            .format("Admission major with id '%d'", admissionMajorId)));
            AdmissionPlan admissionPlan = datasource.getAdmissionPlan(admissionId)
                    .orElseThrow(() -> new AdmissionPlanNotFoundException(String
                            .format("Admission plan with id '%d' not found.", admissionId)));
            if (!admissionPlan.getAdmissionMajors().contains(admissionMajor)) {
                throw new AdmissionMajorNotInCurrentPlanException(String
                        .format("Admission major with id '%d' not in the current plan.", admissionMajorId));
            }
            if (admissionPlan.getAdmissionMajors().stream()
                    .anyMatch(am -> StringUtils.equals(am.getName(), request.getName()))) {
                throw new DuplicateAdmissionMajorNameException(String
                        .format("Admission training name '%s' already exists.", request.getName()));
            }
            AdmissionTrainingProgram admissionTrainingProgram = datasource
                    .getAdmissionTrainingProgram(request.getAdmissionTrainingProgramId(), admissionId)
                    .orElseThrow(() -> new AdmissionTrainingProgramNotFoundException(String
                            .format("Admission training program with id '%d' and belongs to admission plan '%d' not found.",
                                    request.getAdmissionTrainingProgramId(), admissionId)));
            if (!admissionPlan.getAdmissionTrainingPrograms().contains(admissionTrainingProgram)) {
                throw new AdmissionTrainingProgramNotInAdmissionPlanException(String
                        .format("Training program with id '%d' is not in the current admission plan.",
                                admissionTrainingProgram.getId()));
            }
            Major major = datasource.getMajor(request.getMajorId())
                    .orElseThrow(() -> new MajorNotFoundException(String
                            .format("Major with id '%d' not found.", request.getMajorId())));
            admissionMajor.setName(StringUtils.isBlank(request.getName())
                    ? major.getName()
                    : StringUtils.trim(request.getName()));
            admissionMajor.setDescription(StringUtils.trim(request.getDescription()));
            admissionMajor.setQuota(request.getQuota());
            admissionMajor.setMajor(major);
            admissionMajor.setAdmissionTrainingProgram(admissionTrainingProgram);
            admissionMajor = datasource.save(admissionMajor);
            return mapper.toDto(datasource.save(admissionMajor));
        }
    }

    @Datasource
    public static final class UpdateAdmissionMajorDatasource {

        private final AdmissionPlanRepository admissionPlanRepository;
        private final MajorRepository majorRepository;
        private final AdmissionTrainingProgramRepository admissionTrainingProgramRepository;
        private final AdmissionMajorRepository admissionMajorRepository;

        @Autowired
        public UpdateAdmissionMajorDatasource(
                AdmissionPlanRepository admissionPlanRepository,
                MajorRepository majorRepository,
                AdmissionTrainingProgramRepository admissionTrainingProgramRepository,
                AdmissionMajorRepository admissionMajorRepository) {
            this.admissionPlanRepository = admissionPlanRepository;
            this.majorRepository = majorRepository;
            this.admissionTrainingProgramRepository = admissionTrainingProgramRepository;
            this.admissionMajorRepository = admissionMajorRepository;
        }

        public Optional<Major> getMajor(Long majorId) {
            return majorRepository.findById(majorId);
        }

        public Optional<AdmissionTrainingProgram> getAdmissionTrainingProgram(Long id, Long admissionId) {
            return admissionTrainingProgramRepository.getByIdAndAdmissionPlanId(id, admissionId);
        }

        public AdmissionTrainingProgram update(AdmissionTrainingProgram admissionTrainingProgram) {
            return admissionTrainingProgramRepository.save(admissionTrainingProgram);
        }

        public AdmissionMajor save(AdmissionMajor admissionMajor) {
            return admissionMajorRepository.save(admissionMajor);
        }

        public Optional<AdmissionPlan> getAdmissionPlan(Long admissionId) {
            return admissionPlanRepository.getAdmissionPlanMajorsTrainingProgramsPopulated(admissionId);
        }

        public Optional<AdmissionMajor> getAdmissionMajor(Long admissionMajorId) {
            return admissionMajorRepository.findById(admissionMajorId);
        }
    }
}
