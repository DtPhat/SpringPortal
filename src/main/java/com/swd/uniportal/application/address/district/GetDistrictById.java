package com.swd.uniportal.application.address.district;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.District;
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
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetDistrictById {

    @Builder
    public record DistrictResponse(Long id, String name, List<WardResponse> wards) {
    }

    @Builder
    public record WardResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class GetDistrictByIdController extends BaseController {

        private final GetDistrictByIdService service;

        @GetMapping("/addresses/wards/districts/{id}")
        @Operation(summary = "Get a district based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DistrictResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "404",
                description = "District not found.",
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
        public ResponseEntity<Object> get(@PathVariable("id") Long id) {
            try {
                return ResponseEntity.ok(service.get(id));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class GetDistrictByIdService {

        private final GetDistrictByIdDatasource datasource;

        public DistrictResponse get(Long id) throws Exception {
            Optional<District> districtOptional = datasource.findById(id);
            if (districtOptional.isEmpty()) {
                throw new Exception("District not found");
            }
            District district = districtOptional.get();

            List<WardResponse> wardResponses = district.getWards().stream()
                    .map(ward -> new WardResponse(ward.getId(), ward.getName()))
                    .collect(Collectors.toList());

            return new DistrictResponse(district.getId(), district.getName(), wardResponses);
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetDistrictByIdDatasource {

        private final DistrictRepository districtRepository;
        private final WardRepository wardRepository;

        public Optional<District> findById(Long id) {
            return districtRepository.findByIdWithWards(id);
        }

    }

}
