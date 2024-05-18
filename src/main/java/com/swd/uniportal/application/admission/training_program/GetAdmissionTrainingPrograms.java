package com.swd.uniportal.application.admission.training_program;

import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramsDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionTrainingProgram;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
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
public final class GetAdmissionTrainingPrograms {

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetAdmissionTrainingProgramsController extends BaseController {

        private final GetAdmissionTrainingProgramsService service;

        @Autowired
        public GetAdmissionTrainingProgramsController(GetAdmissionTrainingProgramsService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans/{admissionId}/training-programs")
        @Operation(summary = "Get admission training programs.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionTrainingProgramsDto.class)
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
                @PathVariable("admissionId") Long admissionId,
                @RequestParam(value = "search", defaultValue = "") String search,
                @RequestParam(value = "page", defaultValue = "1") Integer page
        ) {
            if (page < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Page must be positive.")));
            }
            try {
                AdmissionTrainingProgramsDto result = service.get(admissionId, page, search);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetAdmissionTrainingProgramsService {

        private final GetAdmissionTrainingProgramsDatasource datasource;
        private final Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> mapper;

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        @Autowired
        public GetAdmissionTrainingProgramsService(
                GetAdmissionTrainingProgramsDatasource datasource,
                Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionTrainingProgramsDto get(Long admissionId, Integer page, String search) {
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            List<AdmissionTrainingProgram> admissionTrainingPrograms = datasource.getBySearch(admissionId, search, pageable);
            Integer countAll = datasource.countAllBySearch(admissionId, search);
            return AdmissionTrainingProgramsDto.builder()
                    .page(page)
                    .totalPages(Math.ceilDiv(countAll, pageSize))
                    .size(admissionTrainingPrograms.size())
                    .pageSize(pageSize)
                    .admissionTrainingPrograms(admissionTrainingPrograms.stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetAdmissionTrainingProgramsDatasource {

        private final AdmissionTrainingProgramRepository admissionTrainingProgramRepository;

        @Autowired
        public GetAdmissionTrainingProgramsDatasource(
                AdmissionTrainingProgramRepository admissionTrainingProgramRepository) {
            this.admissionTrainingProgramRepository = admissionTrainingProgramRepository;
        }

        public Integer countAllBySearch(Long admissionId, String search) {
            return admissionTrainingProgramRepository
                    .countAdmissionTrainingProgramsBySearch(admissionId, search);
        }

        public List<AdmissionTrainingProgram> getBySearch(Long admissionId, String search, Pageable pageable) {
            return admissionTrainingProgramRepository
                    .getAdmissionTrainingProgramsByAdmissionPlanAndSearch(admissionId, search, pageable);
        }
    }
}
