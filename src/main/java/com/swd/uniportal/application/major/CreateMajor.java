package com.swd.uniportal.application.major;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.major.exception.DuplicatedCodeException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.major.Department;
import com.swd.uniportal.domain.major.Major;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.DepartmentRepository;
import com.swd.uniportal.infrastructure.repository.MajorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateMajor {

    @Data
    public static class CreateMajorRequest {

        @NotBlank(message = "name: must not be null or blank.")
        private String name;

        @NotBlank(message = "code: must not be null or blank.")
        private String code;

        private String description;

        @NotNull(message = "department id: must not be null.")
        private Long departmentId; // Add field for department ID
    }

    @Builder
    public record MajorCreatedResponse(Long id, String name, String code, String description, Long departmentId) {
    }

    @RestController
    @AllArgsConstructor
    @Tag(name = "majors")
    public static class CreateMajorController extends BaseController {

        private final CreateMajorService service;

        @PostMapping("/majors")
        @Operation(summary = "Create a major.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Major info to create.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreateMajorRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MajorCreatedResponse.class)
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
        public ResponseEntity<Object> create(@RequestBody CreateMajorRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                MajorCreatedResponse response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (DuplicatedCodeException | AccountNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }

    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class CreateMajorService {

        private final CreateMajorDatasource datasource;

        public MajorCreatedResponse create(CreateMajorRequest request) throws AccountNotFoundException, DuplicatedCodeException {
            Optional<Department> optionalDepartment = datasource.getDepartmentById(request.getDepartmentId());
            if (optionalDepartment.isEmpty()) {
                throw new IllegalArgumentException("Department with the provided ID not found");
            }
            Department department = optionalDepartment.get();

            Major major = Major.builder()
                    .name(StringUtils.trim(request.getName()))
                    .code(StringUtils.trim(request.getCode()))
                    .description(StringUtils.trimToNull(request.getDescription()))
                    .department(department)
                    .build();
            if (datasource.codeAlreadyExists(major.getCode())) {
                throw new DuplicatedCodeException(String.format("Code %s already exists.", major.getCode()));
            }
            major = datasource.persist(major);

            return MajorCreatedResponse.builder()
                    .id(major.getId())
                    .code(major.getCode())
                    .name(major.getName())
                    .description(major.getDescription())
                    .departmentId(department.getId())
                    .build();
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class CreateMajorDatasource {

        private final MajorRepository majorRepository;
        private final AccountRepository accountRepository;
        private final DepartmentRepository departmentRepository;

        public Optional<Department> getDepartmentById(Long id){
            return departmentRepository.findById(id);
        }
        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public boolean codeAlreadyExists(String code) {
            return majorRepository.existsMajorByCode(code);
        }

        public Major persist(Major major) {
            return majorRepository.save(major);
        }
    }
}
