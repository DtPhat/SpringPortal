package com.swd.uniportal.application.admission;

import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionPlanDto;
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
import java.util.HashSet;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddAdmissionPlan {

    @RestController
    @Tag(name = "admission-plans")
    public static final class AddAdmissionPlanController extends BaseController {

        private final AddAdmissionPlanService service;

        @Autowired
        public AddAdmissionPlanController(AddAdmissionPlanService service) {
            this.service = service;
        }

        @PostMapping("/admission-plans")
        @Operation(summary = "Add admission plan.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission plan to add.",
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
        public ResponseEntity<Object> add(
                @RequestBody ModifyAdmissionPlanDto request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                AdmissionPlanDto updated = service.update(request);
                return ResponseEntity.ok(updated);
            } catch (InstitutionNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static final class AddAdmissionPlanService {

        private final AddAdmissionPlanDatasource datasource;
        private final Mapper<AdmissionPlan, AdmissionPlanDto> mapper;

        @Autowired
        public AddAdmissionPlanService(
                AddAdmissionPlanDatasource datasource,
                Mapper<AdmissionPlan, AdmissionPlanDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionPlanDto update(ModifyAdmissionPlanDto request) throws InstitutionNotFoundException {
            if (datasource.institutionDoesNotExist(request.getInstitutionId())) {
                throw new InstitutionNotFoundException(String
                        .format("Institution with id '%d' not found.", request.getInstitutionId()));
            }
            AdmissionPlan admissionPlan = AdmissionPlan.builder()
                    .name(StringUtils.trim(request.getName()))
                    .description(StringUtils.trim(request.getDescription()))
                    .year(request.getYear())
                    .institution(datasource.getInstitutionReference(request.getInstitutionId()))
                    .admissionMajors(new HashSet<>())
                    .admissionTrainingPrograms(new HashSet<>())
                    .build();
            admissionPlan = datasource.save(admissionPlan);
            return mapper.toDto(admissionPlan);
        }
    }

    @Datasource
    public static final class AddAdmissionPlanDatasource {

        private final AdmissionPlanRepository admissionPlanRepository;
        private final InstitutionRepository institutionRepository;

        @Autowired
        public AddAdmissionPlanDatasource(
                AdmissionPlanRepository admissionPlanRepository,
                InstitutionRepository institutionRepository) {
            this.admissionPlanRepository = admissionPlanRepository;
            this.institutionRepository = institutionRepository;
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
