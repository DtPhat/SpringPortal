package com.swd.uniportal.application.admission;

import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionPlanDto;
import com.swd.uniportal.application.admission.exception.AdmissionPlanNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.institution.exception.InstitutionNotFoundException;
import com.swd.uniportal.domain.admission.AdmissionPlan;
import com.swd.uniportal.domain.institution.Institution;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionPlanRepository;
import com.swd.uniportal.infrastructure.repository.InstitutionRepository;
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
public final class UpdateAdmissionPlan {

    @RestController
    @Tag(name = "admission-plans")
    public static class UpdateAdmissionPlanController extends BaseController {

        private final UpdateAdmissionPlanService service;

        @Autowired
        public UpdateAdmissionPlanController(UpdateAdmissionPlanService service) {
            this.service = service;
        }

        @PutMapping("/admission-plans/{id}")
        @Operation(summary = "Update admission plan.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission plan to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ModifyAdmissionPlanDto.class)
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
                @PathVariable("id") Long id,
                @RequestBody ModifyAdmissionPlanDto request) {
            if (id < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission plan id must be positive.")));
            }
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                AdmissionPlanDto updated = service.update(id, request);
                return ResponseEntity.ok(updated);
            } catch (AdmissionPlanNotFoundException | InstitutionNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static class UpdateAdmissionPlanService {

        private final UpdateAdmissionPlanDatasource datasource;
        private final Mapper<AdmissionPlan, AdmissionPlanDto> mapper;

        @Autowired
        public UpdateAdmissionPlanService(
                UpdateAdmissionPlanDatasource datasource,
                Mapper<AdmissionPlan, AdmissionPlanDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionPlanDto update(Long id, ModifyAdmissionPlanDto request) throws AdmissionPlanNotFoundException, InstitutionNotFoundException {
            AdmissionPlan admissionPlan = datasource.getById(id)
                    .orElseThrow(() -> new AdmissionPlanNotFoundException(String
                            .format("Admission plan with id '%d' not found.", id)));
            admissionPlan.setName(StringUtils.trim(request.getName()));
            admissionPlan.setDescription(StringUtils.trim(request.getDescription()));
            admissionPlan.setYear(request.getYear());
            if (datasource.institutionDoesNotExist(request.getInstitutionId())) {
                throw new InstitutionNotFoundException(String
                        .format("Institution with id '%d' not found.", request.getInstitutionId()));
            }
            admissionPlan.setInstitution(datasource.getInstitutionReference(request.getInstitutionId()));
            admissionPlan = datasource.save(admissionPlan);
            return mapper.toDto(admissionPlan);
        }
    }

    @Datasource
    public static class UpdateAdmissionPlanDatasource {

        private final AdmissionPlanRepository admissionPlanRepository;
        private final InstitutionRepository institutionRepository;

        @Autowired
        public UpdateAdmissionPlanDatasource(
                AdmissionPlanRepository admissionPlanRepository,
                InstitutionRepository institutionRepository) {
            this.admissionPlanRepository = admissionPlanRepository;
            this.institutionRepository = institutionRepository;
        }

        public Optional<AdmissionPlan> getById(Long id) {
            return admissionPlanRepository.getByIdPopulated(id);
        }

        public boolean institutionDoesNotExist(Long institutionId) {
            return !institutionRepository.existsById(institutionId);
        }

        public Institution getInstitutionReference(Long institutionId) {
            return institutionRepository.getReferenceById(institutionId);
        }

        public AdmissionPlan save(AdmissionPlan admissionPlan) {
            return admissionPlanRepository.save(admissionPlan);
        }
    }
}
