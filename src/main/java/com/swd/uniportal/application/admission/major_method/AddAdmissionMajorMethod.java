package com.swd.uniportal.application.admission.major_method;

import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.exception.AdmissionMajorNotFoundException;
import com.swd.uniportal.application.admission.exception.AdmissionMethodNotFound;
import com.swd.uniportal.application.admission.exception.DuplicatedAdmissionMajorMethodNameException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.subject.exception.SubjectGroupNotFoundException;
import com.swd.uniportal.domain.admission.AdmissionMajor;
import com.swd.uniportal.domain.admission.AdmissionMajorMethod;
import com.swd.uniportal.domain.admission.AdmissionMethod;
import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.subject.SubjectGroup;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorMethodRepository;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorRepository;
import com.swd.uniportal.infrastructure.repository.AdmissionMethodRepository;
import com.swd.uniportal.infrastructure.repository.SubjectGroupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
public final class AddAdmissionMajorMethod {

    @RestController
    @Tag(name = "admission-plans")
    public static final class AddAdmissionMajorMethodController extends BaseController {

        private final AddAdmissionMajorMethodService service;

        @Autowired
        public AddAdmissionMajorMethodController(AddAdmissionMajorMethodService service) {
            this.service = service;
        }

        @PostMapping("/admission-plans/majors/{admissionMajorId}/methods")
        @Operation(summary = "Add admission major method.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission major method to add.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ModifyAdmissionMajorMethodDto.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AdmissionMajorMethodDto.class)
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
                @PathVariable("admissionMajorId") Long admissionMajorId,
                @RequestBody ModifyAdmissionMajorMethodDto request
        ) {
            if (admissionMajorId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission major id is must be positive.")));
            }
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            if (Objects.isNull(request.getSubjectGroupIds())) {
                request.setSubjectGroupIds(Collections.emptyList());
            }
            request.setSubjectGroupIds(request.getSubjectGroupIds().stream().filter(Objects::nonNull).toList());
            if (request.getSubjectGroupIds().stream().anyMatch(sg -> (sg < 1))) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List
                                .of("Subject group ids must be positive.")));
            }
            request.setName(StringUtils.trim(request.getName()));
            try {
                AdmissionMajorMethodDto added = service.add(admissionMajorId, request);
                return ResponseEntity.ok(added);
            } catch (AdmissionMajorNotFoundException
                    | DuplicatedAdmissionMajorMethodNameException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.")));
            }
        }
    }

    @Service
    public static final class AddAdmissionMajorMethodService {

        private final AddAdmissionMajorMethodDatasource datasource;
        private final Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> mapper;

        @Autowired
        public AddAdmissionMajorMethodService(
                AddAdmissionMajorMethodDatasource datasource,
                Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public AdmissionMajorMethodDto add(Long admissionMajorId, ModifyAdmissionMajorMethodDto request)
                throws AdmissionMajorNotFoundException,
                DuplicatedAdmissionMajorMethodNameException,
                AdmissionMethodNotFound,
                SubjectGroupNotFoundException {
            AdmissionMajor admissionMajor = datasource.getAdmissionMajor(admissionMajorId)
                    .orElseThrow(() -> new AdmissionMajorNotFoundException(String
                            .format("Admission major with id '%d' not found.", admissionMajorId)));
            if (admissionMajor.getAdmissionMajorMethods().stream()
                    .anyMatch(amm -> StringUtils.equals(amm.getName(), request.getName()))) {
                throw new DuplicatedAdmissionMajorMethodNameException();
            }
            AdmissionMethod admissionMethod = datasource.getAdmissionMethod(request.getAdmissionMethodId())
                    .orElseThrow(() -> new AdmissionMethodNotFound(String
                            .format("Admission method with id '%d' not found.", request.getAdmissionMethodId())));
            Set<SubjectGroup> subjectGroups = datasource.getSubjectGroups(request.getSubjectGroupIds());
            if (subjectGroups.size() != request.getSubjectGroupIds().size()) {
                Set<Long> requestedIds = new HashSet<>(request.getSubjectGroupIds());
                requestedIds.removeAll(subjectGroups.stream().map(BaseEntity::getId).collect(Collectors.toSet()));
                throw new SubjectGroupNotFoundException(String
                        .format("Subject group with ids '%s' not found.", requestedIds.stream()
                                .map(Objects::toString)
                                .collect(Collectors.joining(","))));
            }
            AdmissionMajorMethod admissionMajorMethod = AdmissionMajorMethod.builder()
                    .name(request.getName())
                    .admissionMethod(admissionMethod)
                    .subjectGroups(subjectGroups)
                    .admissionMajor(admissionMajor)
                    .build();
            admissionMajorMethod = datasource.save(admissionMajorMethod);
            return mapper.toDto(admissionMajorMethod);
        }
    }

    @Datasource
    public static final class AddAdmissionMajorMethodDatasource {

        private final AdmissionMajorMethodRepository admissionMajorMethodRepository;
        private final AdmissionMajorRepository admissionMajorRepository;
        private final AdmissionMethodRepository admissionMethodRepository;
        private final SubjectGroupRepository subjectGroupRepository;

        @Autowired
        public AddAdmissionMajorMethodDatasource(
                AdmissionMajorMethodRepository admissionMajorMethodRepository,
                AdmissionMajorRepository admissionMajorRepository,
                AdmissionMethodRepository admissionMethodRepository,
                SubjectGroupRepository subjectGroupRepository) {
            this.admissionMajorMethodRepository = admissionMajorMethodRepository;
            this.admissionMajorRepository = admissionMajorRepository;
            this.admissionMethodRepository = admissionMethodRepository;
            this.subjectGroupRepository = subjectGroupRepository;
        }

        public Optional<AdmissionMajor> getAdmissionMajor(Long admissionMajorId) {
            return admissionMajorRepository.getWithMajorMethodsPopulated(admissionMajorId);
        }

        public AdmissionMajorMethod save(AdmissionMajorMethod admissionMajorMethod) {
            return admissionMajorMethodRepository.save(admissionMajorMethod);
        }

        public Optional<AdmissionMethod> getAdmissionMethod(Long admissionMethodId) {
            return admissionMethodRepository.findById(admissionMethodId);
        }

        public Set<SubjectGroup> getSubjectGroups(List<Long> subjectGroupIds) {
            return subjectGroupRepository.getByIdIn(subjectGroupIds);
        }
    }
}
