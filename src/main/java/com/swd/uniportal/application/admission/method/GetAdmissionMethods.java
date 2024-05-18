package com.swd.uniportal.application.admission.method;

import com.swd.uniportal.application.admission.dto.AdmissionMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionMethodsDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMethod;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMethodRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetAdmissionMethods {

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetAdmissionMethodsController extends BaseController {

        private final GetAdmissionMethodsService service;

        @Autowired
        private GetAdmissionMethodsController(GetAdmissionMethodsService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans/methods")
        @Operation(summary = "Get admission methods.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionMethodsDto.class)
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
                @RequestParam(name = "all", defaultValue = "false") boolean all) {
            if (page < 1) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List
                                .of("Id must be positive.")));
            }
            try {
                AdmissionMethodsDto admissionMethods = service.get(search, page, all);
                return ResponseEntity.ok(admissionMethods);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetAdmissionMethodsService {

        private final GetAdmissionMethodsDatasource datasource;
        private final Mapper<AdmissionMethod, AdmissionMethodDto> mapper;

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        @Autowired
        public GetAdmissionMethodsService(
                GetAdmissionMethodsDatasource datasource,
                Mapper<AdmissionMethod, AdmissionMethodDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionMethodsDto get(String search, Integer page, boolean all) {
            List<AdmissionMethod> admissionTrainingPrograms;
            if (all) {
                admissionTrainingPrograms = datasource.getBySearch(search);
            } else {
                Pageable pageable = PageRequest.of(page - 1, pageSize);
                admissionTrainingPrograms = datasource.getBySearch(search, pageable);
            }
            Integer countAll = datasource.countAllBySearch(search);
            return AdmissionMethodsDto.builder()
                    .page(page)
                    .totalPages(Math.ceilDiv(countAll, all ? countAll : pageSize))
                    .size(admissionTrainingPrograms.size())
                    .pageSize(all ? countAll : pageSize)
                    .admissionMethods(admissionTrainingPrograms.stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetAdmissionMethodsDatasource {

        private final AdmissionMethodRepository admissionMethodRepository;

        @Autowired
        public GetAdmissionMethodsDatasource(AdmissionMethodRepository admissionMethodRepository) {
            this.admissionMethodRepository = admissionMethodRepository;
        }

        public Integer countAllBySearch(String search) {
            return admissionMethodRepository
                    .countBySearch(search);
        }

        public List<AdmissionMethod> getBySearch(String search, Pageable pageable) {
            return admissionMethodRepository
                    .getBySearch(search, pageable);
        }

        public List<AdmissionMethod> getBySearch(String search) {
            return admissionMethodRepository.getBySearch(search);
        }
    }
}
