package com.swd.uniportal.application.admission.major_method;

import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodsDto;
import com.swd.uniportal.application.admission.training_program.GetTrainingPrograms;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMajorMethod;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorMethodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetAdmissionMajorMethods {

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetAdmissionMajorMethodsController extends BaseController {

        private final GetAdmissionMajorMethodsService service;

        @Autowired
        public GetAdmissionMajorMethodsController(GetAdmissionMajorMethodsService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans/majors/{admissionMajorId}/methods")
        @Operation(summary = "Get admission major methods.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GetTrainingPrograms.TrainingProgramsDto.class)
                )
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
        public ResponseEntity<Object> get(
                @PathVariable("admissionMajorId") Long majorId,
                @RequestParam(name = "search", defaultValue = "") String search,
                @RequestParam(name = "page", defaultValue = "1") Integer page) {
            if (majorId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Id must be positive.")));
            }
            if (page < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Page must be positive.")));
            }
            try {
                AdmissionMajorMethodsDto admissionMajorMethods = service.get(majorId, search, page);
                return ResponseEntity.ok(admissionMajorMethods);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetAdmissionMajorMethodsService {

        private final GetAdmissionMajorMethodsDatasource datasource;
        private final Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> mapper;

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        @Autowired
        public GetAdmissionMajorMethodsService(
                GetAdmissionMajorMethodsDatasource datasource,
                Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionMajorMethodsDto get(Long majorId, String search, Integer page) {
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            List<AdmissionMajorMethod> admissionTrainingPrograms = datasource.getBySearch(majorId, search, pageable);
            Integer countAll = datasource.countAllBySearch(majorId, search);
            return AdmissionMajorMethodsDto.builder()
                    .page(page)
                    .totalPages(Math.ceilDiv(countAll, pageSize))
                    .size(admissionTrainingPrograms.size())
                    .pageSize(pageSize)
                    .admissionMajorMethods(admissionTrainingPrograms.stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetAdmissionMajorMethodsDatasource {

        private final AdmissionMajorMethodRepository admissionMajorMethodRepository;

        @Autowired
        public GetAdmissionMajorMethodsDatasource(AdmissionMajorMethodRepository admissionMajorMethodRepository) {
            this.admissionMajorMethodRepository = admissionMajorMethodRepository;
        }

        public List<AdmissionMajorMethod> getBySearch(Long majorId, String search, Pageable pageable) {
            return admissionMajorMethodRepository.getBySearch(majorId, search, pageable);
        }

        public Integer countAllBySearch(Long majorId, String search) {
            return admissionMajorMethodRepository.countBySearch(majorId, search);
        }
    }
}
