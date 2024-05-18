package com.swd.uniportal.application.address.city_province;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCityProvinceById {

    @Data
    public static class UpdateCityProvinceRequest {

        @NotBlank(message = "name must not be null or blank.")
        private String name;

    }

    @Builder
    public record CityProvinceUpdatedResponse(Long id, String name) {}

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class UpdateCityProvinceController extends BaseController {

        private final UpdateCityProvinceService service;

        @PutMapping("/addresses/wards/districts/city-provinces/{id}")
        @Operation(summary = "Update a city or province by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "City or province info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateCityProvinceRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CityProvinceUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or City/Province not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateCityProvinceRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                CityProvinceUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateCityProvinceService {

        private final UpdateCityProvinceDatasource datasource;

        public CityProvinceUpdatedResponse update(Long id, UpdateCityProvinceRequest request) throws Exception {
            Optional<CityProvince> cityProvinceOptional = datasource.findById(id);
            if (cityProvinceOptional.isEmpty()) {
                throw new Exception("City Province not found");
            }
            CityProvince cityProvince = cityProvinceOptional.get();

            cityProvince.setName(request.getName());

            CityProvince updatedCityProvince = datasource.update(cityProvince);

            return new CityProvinceUpdatedResponse(updatedCityProvince.getId(), updatedCityProvince.getName());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateCityProvinceDatasource {

        private final CityProvinceRepository cityProvinceRepository;

        public Optional<CityProvince> findById(Long id) {
            return cityProvinceRepository.findById(id);
        }

        public CityProvince update(CityProvince cityProvince) {
            return cityProvinceRepository.save(cityProvince);
        }
    }
}
