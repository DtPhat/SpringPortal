package com.swd.uniportal.application.admission;

import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.exception.AdmissionPlanNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionPlan;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetAdmissionPlan {

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetAdmissionPlanController extends BaseController {

        private final GetAdmissionPlanService service;

        @Autowired
        public GetAdmissionPlanController(GetAdmissionPlanService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans/{id}")
        @Operation(summary = "Get admission plan.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionPlanDto.class)
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
        public ResponseEntity<Object> get(@PathVariable("id") Long id) {
            if (id < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Id is not positive.")));
            }
            try {
                AdmissionPlanDto admissionPlan = service.get(id);
                return ResponseEntity.ok(admissionPlan);
            } catch (AdmissionPlanNotFoundException e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetAdmissionPlanService {

        private final GetAdmissionPlanDatasource datasource;
        private final Mapper<AdmissionPlan, AdmissionPlanDto> mapper;

        @Autowired
        public GetAdmissionPlanService(
                GetAdmissionPlanDatasource datasource,
                Mapper<AdmissionPlan, AdmissionPlanDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionPlanDto get(Long id) throws AdmissionPlanNotFoundException {
            AdmissionPlan admissionPlan = datasource.getById(id)
                    .orElseThrow(() -> new AdmissionPlanNotFoundException(String
                            .format("Admission plan with id '%d' not found.", id)));
            return mapper.toDto(admissionPlan);
        }
    }

    @Datasource
    public static final class GetAdmissionPlanDatasource {

        private final AdmissionPlanRepository repository;

        @Autowired
        public GetAdmissionPlanDatasource(AdmissionPlanRepository repository) {
            this.repository = repository;
        }

        public Optional<AdmissionPlan> getById(Long id) {
            return repository.getByIdPopulated(id);
        }
    }
}
