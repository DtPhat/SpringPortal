package com.swd.uniportal.application.address.city_province;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
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
public class CreateCityProvince {

    @Data
    public static class CreateCityProvinceRequest {

        @NotBlank(message = "name must not be null or blank.")
        private String name;

    }

    @Builder
    public record CityProvinceCreatedResponse(Long id, String name) {}

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class CreateCityProvinceController extends BaseController {

        private final CreateCityProvinceService service;

        @PostMapping("/addresses/wards/districts/city-provinces")
        @Operation(summary = "Create a city or province.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "City or province info to create.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreateCityProvinceRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CityProvinceCreatedResponse.class)
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
        public ResponseEntity<Object> create(@RequestBody CreateCityProvinceRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                CityProvinceCreatedResponse response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class CreateCityProvinceService {

        private final CreateCityProvinceDatasource datasource;

        public CityProvinceCreatedResponse create(CreateCityProvinceRequest request) throws AccountNotFoundException {
            CityProvince cityProvince = CityProvince.builder()
                    .name(StringUtils.trim(request.getName()))
                    .build();

            CityProvince createdCityProvince = datasource.persist(cityProvince);

            return new CityProvinceCreatedResponse(createdCityProvince.getId(), createdCityProvince.getName());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class CreateCityProvinceDatasource {

        private final CityProvinceRepository cityProvinceRepository;
        private final AccountRepository accountRepository;

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public CityProvince persist(CityProvince cityProvince) {
            return cityProvinceRepository.save(cityProvince);
        }
    }
}