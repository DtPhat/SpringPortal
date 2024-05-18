package com.swd.uniportal.application.address.district;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.domain.address.District;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import com.swd.uniportal.infrastructure.repository.DistrictRepository;
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
public class CreateDistrict {

    @Data
    public static class CreateDistrictRequest {

        @NotBlank(message = "name must not be null or blank.")
        private String name;

        @NotNull(message = "cityProvinceId must not be null or blank.")
        private Long cityProvinceId;

    }

    @Builder
    public record DistrictCreatedResponse(Long id, String name, Long cityProvinceId) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class CreateDistrictController extends BaseController {

        private final CreateDistrictService service;

        @PostMapping("/addresses/wards/districts")
        @Operation(summary = "Create a district.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "District info to create.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateDistrictRequest.class)))
        @ApiResponse(responseCode = "201", description = "Successfully created.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DistrictCreatedResponse.class)))
        @ApiResponse(responseCode = "400", description = "Invalid request body.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        @ApiResponse(responseCode = "500", description = "Server error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        public ResponseEntity<Object> create(@RequestBody CreateDistrictRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                System.out.println(violations.getFirst());
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                DistrictCreatedResponse response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class CreateDistrictService {

        private final CreateDistrictDatasource datasource;

        public DistrictCreatedResponse create(CreateDistrictRequest request) throws AccountNotFoundException {
            CityProvince cityProvince = datasource.getCityProvinceById(request.getCityProvinceId()).orElseThrow(() -> new IllegalArgumentException("City province not found"));

            District district = District.builder().name(StringUtils.trim(request.getName())).cityProvince(cityProvince).build();

            District createdDistrict = datasource.persist(district);

            return new DistrictCreatedResponse(createdDistrict.getId(), createdDistrict.getName(), cityProvince.getId());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class CreateDistrictDatasource {

        private final DistrictRepository districtRepository;
        private final CityProvinceRepository cityProvinceRepository;
        private final AccountRepository accountRepository;

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public Optional<CityProvince> getCityProvinceById(Long id) {
            return cityProvinceRepository.findById(id);
        }

        public District persist(District district) {
            return districtRepository.save(district);
        }
    }
}