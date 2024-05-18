package com.swd.uniportal.application.admission;

import com.swd.uniportal.application.admission.dto.InstitutionDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.admission.AdmissionPlan;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetAdmissionPlans {

    @Data
    @Builder
    public static final class AdmissionPlansDto {

        Integer page;
        Integer totalPages;
        Integer pageSize;
        Integer size;
        List<SimpleAdmissionPlanDto> admissionPlans;
    }

    @Data
    @Builder
    public static final class SimpleAdmissionPlanDto {

        Long id;
        String name;
        Integer year;
        InstitutionDto institution;
    }

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetAdmissionPlansController extends BaseController {

        private final GetAdmissionPlansService service;

        @Autowired
        public GetAdmissionPlansController(GetAdmissionPlansService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans")
        @Operation(summary = "Get admission plans.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionPlansDto.class)
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
        public ResponseEntity<Object> get(
                @RequestParam(name = "search", defaultValue = "") String search,
                @RequestParam(name = "page", defaultValue = "1") Integer page,
                @RequestParam(name = "institutionId", required = false) Long institutionId) {
            Integer pageToUse = page;
            if (Objects.isNull(pageToUse) || pageToUse < 1) {
                pageToUse = 1;
            }
            try {
                AdmissionPlansDto admissionPlan = service.get(search, pageToUse, institutionId);
                return ResponseEntity.ok(admissionPlan);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetAdmissionPlansService {

        private final GetAdmissionPlansDataSource datasource;

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        @Autowired
        public GetAdmissionPlansService(GetAdmissionPlansDataSource datasource) {
            this.datasource = datasource;
        }

        public AdmissionPlansDto get(String search, Integer pageToUse, Long institutionId) {
            Pageable pageable = PageRequest.of(pageToUse - 1, pageSize);
            List<AdmissionPlan> admissionPlans;
            if (Objects.isNull(institutionId)) {
                admissionPlans = datasource.getBySearch(search, pageable);
            } else {
                admissionPlans = datasource.getBySearchAndInstitution(search, pageable, institutionId);
            }
            List<SimpleAdmissionPlanDto> admissionPlanList = admissionPlans.stream()
                    .map(ap -> SimpleAdmissionPlanDto.builder()
                            .id(ap.getId())
                            .year(ap.getYear())
                            .name(ap.getName())
                            .institution(InstitutionDto.builder()
                                    .id(ap.getInstitution().getId())
                                    .name(ap.getInstitution().getName())
                                    .code(ap.getInstitution().getCode())
                                    .avatarLink(ap.getInstitution().getAvatarLink())
                                    .build())
                            .build()).toList();
            Integer count = Objects.isNull(institutionId)
                    ? Math.ceilDiv(datasource.countAdmissionPlansBySearch(search), pageSize)
                    : Math.ceilDiv(datasource.countAdmissionPlansBySearchAndInstitutionId(search, institutionId), pageSize);
            return AdmissionPlansDto.builder()
                    .page(pageToUse)
                    .totalPages(count)
                    .pageSize(pageSize)
                    .size(admissionPlanList.size())
                    .admissionPlans(admissionPlanList)
                    .build();
        }
    }

    @Datasource
    public static final class GetAdmissionPlansDataSource {

        private final AdmissionPlanRepository repository;

        @Autowired
        public GetAdmissionPlansDataSource(AdmissionPlanRepository repository) {
            this.repository = repository;
        }

        public List<AdmissionPlan> getBySearch(String search, Pageable pageable) {
            return repository.getAdmissionPlansBySearch(search, pageable);
        }

        public List<AdmissionPlan> getBySearchAndInstitution(String search, Pageable pageable, Long institutionId) {
            return repository.getAdmissionPlansBySearchAndInstitutionId(search, institutionId, pageable);
        }

        public int countAdmissionPlansBySearch(String search) {
            return repository.countAdmissionPlansBySearch(search);
        }

        public int countAdmissionPlansBySearchAndInstitutionId(String search, Long institutionId) {
            return repository.countAdmissionPlansBySearchAndInstitutionId(search, institutionId);
        }
    }
}
