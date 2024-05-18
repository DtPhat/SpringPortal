package com.swd.uniportal.application.admission.major;

import com.swd.uniportal.application.admission.dto.AdmissionMajorDto;
import com.swd.uniportal.application.admission.dto.AdmissionMajorsDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMajor;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorRepository;
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
public final class GetAdmissionMajors {

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetAdmissionMajorsController extends BaseController {

        private final GetAdmissionMajorsService service;

        @Autowired
        public GetAdmissionMajorsController(GetAdmissionMajorsService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans/{admissionId}/majors")
        @Operation(summary = "Get admission majors.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionMajorsDto.class)
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
                AdmissionMajorsDto result = service.get(admissionId, page, search);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetAdmissionMajorsService {

        private final GetAdmissionMajorsDatasource datasource;
        private final Mapper<AdmissionMajor, AdmissionMajorDto> mapper;

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        @Autowired
        public GetAdmissionMajorsService(
                GetAdmissionMajorsDatasource datasource,
                Mapper<AdmissionMajor, AdmissionMajorDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionMajorsDto get(Long admissionId, Integer page, String search) {
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            List<AdmissionMajor> admissionMajors = datasource.getBySearch(admissionId, search, pageable);
            Integer countAll = datasource.countAllBySearch(admissionId, search);
            return AdmissionMajorsDto.builder()
                    .page(page)
                    .totalPages(Math.ceilDiv(countAll, pageSize))
                    .size(admissionMajors.size())
                    .pageSize(pageSize)
                    .admissionMajors(admissionMajors.stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetAdmissionMajorsDatasource {

        private final AdmissionMajorRepository admissionMajorRepository;

        @Autowired
        public GetAdmissionMajorsDatasource(AdmissionMajorRepository admissionMajorRepository) {
            this.admissionMajorRepository = admissionMajorRepository;
        }

        public List<AdmissionMajor> getBySearch(Long admissionId, String search, Pageable pageable) {
            return admissionMajorRepository.getAdmissionMajorsBySearch(admissionId, search, pageable);
        }

        public Integer countAllBySearch(Long admissionId, String search) {
            return admissionMajorRepository.countAllBySearch(admissionId, search);
        }
    }
}
