package com.swd.uniportal.application.address.district;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.domain.address.District;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import com.swd.uniportal.infrastructure.repository.DistrictRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateDistrictById {

    @Data
    public static class UpdateDistrictRequest {

        private String name;

        private Long cityProvinceId;

    }

    @Builder
    public record DistrictUpdatedResponse(Long id, String name, Long cityProvinceId) {}

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class UpdateDistrictController extends BaseController {

        private final UpdateDistrictService service;

        @PutMapping("/addresses/wards/districts/{id}")
        @Operation(summary = "Update a district by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "District info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateDistrictRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DistrictUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or District not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateDistrictRequest request) {
            try {
                DistrictUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateDistrictService {

        private final UpdateDistrictDatasource datasource;

        public DistrictUpdatedResponse update(Long id, UpdateDistrictRequest request) throws Exception {
            Optional<District> districtOptional = datasource.findById(id);
            if (districtOptional.isEmpty()) {
                throw new Exception("District not found");
            }
            District district = districtOptional.get();

            if (StringUtils.isNotBlank(request.getName())) {
                district.setName(StringUtils.trim(request.getName()));
            }

            if (request.getCityProvinceId() != null) {
                district.setCityProvince(datasource.getCityProvinceById(request.getCityProvinceId())
                        .orElseThrow(() -> new Exception("City Province not found")));
            }

            District updatedDistrict = datasource.update(district);

            return new DistrictUpdatedResponse(updatedDistrict.getId(), updatedDistrict.getName(), updatedDistrict.getCityProvince().getId());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateDistrictDatasource {

        private final DistrictRepository districtRepository;
        private final CityProvinceRepository cityProvinceRepository;

        public Optional<District> findById(Long id) {
            return districtRepository.findById(id);
        }

        public Optional<CityProvince> getCityProvinceById(Long id) {
            return cityProvinceRepository.findById(id);
        }

        public District update(District district) {
            return districtRepository.save(district);
        }
    }
}


