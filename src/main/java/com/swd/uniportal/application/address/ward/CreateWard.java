package com.swd.uniportal.application.address.ward;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.address.District;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.DistrictRepository;
import com.swd.uniportal.infrastructure.repository.WardRepository;
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
public class CreateWard {

    @Data
    public static class CreateWardRequest {

        @NotBlank(message = "name must not be null or blank.")
        private String name;

        @NotNull(message = "district id: must not be null.")
        private Long districtId;
    }

    @Builder
    public record WardCreatedResponse(Long id, String name, Long districtId) {
    }

    @RestController
    @AllArgsConstructor
    @Tag(name = "addresses")
    public static class CreateWardController extends BaseController {

        private final CreateWardService service;

        @PostMapping("/addresses/wards")
        @Operation(summary = "Create a ward.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Ward info to create.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreateWardRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = WardCreatedResponse.class)
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
        public ResponseEntity<Object> create(@RequestBody CreateWardRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                WardCreatedResponse response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (AccountNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }

    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class CreateWardService {

        private final CreateWardDatasource datasource;

        public WardCreatedResponse create(CreateWardRequest request) throws AccountNotFoundException {
            Optional<District> optionalDistrict = datasource.getDistrictById(request.getDistrictId());
            if (optionalDistrict.isEmpty()) {
                throw new IllegalArgumentException("District with the provided ID not found");
            }
            District district = optionalDistrict.get();

            Ward ward = Ward.builder()
                    .name(StringUtils.trim(request.getName()))
                    .district(district)
                    .build();

            ward = datasource.persist(ward);

            return new WardCreatedResponse(ward.getId(), ward.getName(), district.getId());
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class CreateWardDatasource {

        private final WardRepository wardRepository;
        private final AccountRepository accountRepository;
        private final DistrictRepository districtRepository;

        public Optional<District> getDistrictById(Long id){
            return districtRepository.findById(id);
        }

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public Ward persist(Ward ward) {
            return wardRepository.save(ward);
        }
    }
}
