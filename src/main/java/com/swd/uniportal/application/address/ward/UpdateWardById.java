package com.swd.uniportal.application.address.ward;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.District;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DistrictRepository;
import com.swd.uniportal.infrastructure.repository.WardRepository;
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
public class UpdateWardById {

    @Data
    public static class UpdateWardRequest {

        private String name;

        private Long districtId;

    }

    @Builder
    public record WardUpdatedResponse(Long id, String name, Long districtId) {}

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class UpdateWardController extends BaseController {

        private final UpdateWardService service;

        @PutMapping("/addresses/wards/{id}")
        @Operation(summary = "Update a ward by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Ward info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateWardRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = WardUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or Ward not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateWardRequest request) {
            try {
                WardUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateWardService {

        private final UpdateWardDatasource datasource;

        public WardUpdatedResponse update(Long id, UpdateWardRequest request) throws Exception {
            Optional<Ward> wardOptional = datasource.findById(id);
            if (wardOptional.isEmpty()) {
                throw new Exception("Ward not found");
            }
            Ward ward = wardOptional.get();

            if (StringUtils.isNotBlank(request.getName())) {
                ward.setName(StringUtils.trim(request.getName()));
            }

            if (request.getDistrictId() != null) {
                ward.setDistrict(datasource.getDistrictById(request.getDistrictId())
                        .orElseThrow(() -> new Exception("District not found")));
            }

            Ward updatedWard = datasource.update(ward);

            return new WardUpdatedResponse(updatedWard.getId(), updatedWard.getName(), updatedWard.getDistrict().getId());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateWardDatasource {

        private final WardRepository wardRepository;
        private final DistrictRepository districtRepository;

        public Optional<Ward> findById(Long id) {
            return wardRepository.findById(id);
        }

        public Optional<District> getDistrictById(Long id) {
            return districtRepository.findById(id);
        }

        public Ward update(Ward ward) {
            return wardRepository.save(ward);
        }
    }
}
