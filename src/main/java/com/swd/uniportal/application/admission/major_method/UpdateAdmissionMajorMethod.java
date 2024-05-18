package com.swd.uniportal.application.admission.major_method;

import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.dto.ModifyAdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.exception.AdmissionMajorMethodNotFound;
import com.swd.uniportal.application.admission.exception.AdmissionMajorMethodNotInAdmissionMajorException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateAdmissionMajorMethod {

    @RestController
    @Tag(name = "admission-plans")
    public static final class UpdateAdmissionMajorMethodController extends BaseController {

        private final UpdateAdmissionMajorMethodService service;

        @Autowired
        public UpdateAdmissionMajorMethodController(UpdateAdmissionMajorMethodService service) {
            this.service = service;
        }

        @PutMapping("/admission-plans/majors/{admissionMajorId}/methods/{admissionMajorMethodId}")
        @Operation(summary = "Update admission major method.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admission major method to update.",
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
                @PathVariable("admissionMajorMethodId") Long admissionMajorMethodId,
                @RequestBody ModifyAdmissionMajorMethodDto request
        ) {
            if (admissionMajorId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission major id is must be positive.")));
            }
            if (admissionMajorMethodId < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Admission major method id is must be positive.")));
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
                AdmissionMajorMethodDto added = service.update(admissionMajorId, admissionMajorMethodId, request);
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
    public static final class UpdateAdmissionMajorMethodService {

        private final UpdateAdmissionMajorMethodDatasource datasource;
        private final Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> mapper;

        @Autowired
        public UpdateAdmissionMajorMethodService(
                UpdateAdmissionMajorMethodDatasource datasource,
                Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        @SuppressWarnings("Duplicates")
        public AdmissionMajorMethodDto update(
                Long admissionMajorId,
                Long admissionMajorMethodId,
                ModifyAdmissionMajorMethodDto request)
                throws DuplicatedAdmissionMajorMethodNameException,
                SubjectGroupNotFoundException,
                AdmissionMajorNotFoundException,
                AdmissionMethodNotFound,
                AdmissionMajorMethodNotFound,
                AdmissionMajorMethodNotInAdmissionMajorException {
            AdmissionMajor admissionMajor = datasource.getAdmissionMajor(admissionMajorId)
                    .orElseThrow(() -> new AdmissionMajorNotFoundException(String
                            .format("Admission major with id '%d' not found.", admissionMajorId)));
            if (admissionMajor.getAdmissionMajorMethods().stream()
                    .anyMatch(amm -> StringUtils.equals(amm.getName(), request.getName()))) {
                throw new DuplicatedAdmissionMajorMethodNameException();
            }
            AdmissionMajorMethod admissionMajorMethod = datasource.getAdmissionMajorMethod(admissionMajorMethodId)
                    .orElseThrow(() -> new AdmissionMajorMethodNotFound(String
                            .format("Admission major method with id '%d' not found.", admissionMajorMethodId)));
            if (!Objects.equals(admissionMajorMethod.getAdmissionMajor(), admissionMajor)) {
                throw new AdmissionMajorMethodNotInAdmissionMajorException(
                        "Admission major method is not in admission major.");
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
            admissionMajorMethod.setName(request.getName());
            admissionMajorMethod.setAdmissionMethod(admissionMethod);
            admissionMajorMethod.setSubjectGroups(subjectGroups);
            admissionMajorMethod = datasource.save(admissionMajorMethod);
            return mapper.toDto(admissionMajorMethod);
        }
    }

    @Datasource
    public static final class UpdateAdmissionMajorMethodDatasource {

        private final AdmissionMajorMethodRepository admissionMajorMethodRepository;
        private final AdmissionMajorRepository admissionMajorRepository;
        private final AdmissionMethodRepository admissionMethodRepository;
        private final SubjectGroupRepository subjectGroupRepository;

        @Autowired
        public UpdateAdmissionMajorMethodDatasource(
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

        public Optional<AdmissionMajorMethod> getAdmissionMajorMethod(Long admissionMajorMethodId) {
            return admissionMajorMethodRepository.getWithSubjectGroupsPopulated(admissionMajorMethodId);
        }
    }
}
