package com.swd.uniportal.application.address.ward;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
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
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetWardById {

    @Builder
    public record WardResponse(Long id, String name, String district, String cityProvince) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class GetWardByIdController extends BaseController {

        private final GetWardByIdService service;

        @GetMapping("/addresses/wards/{id}")
        @Operation(summary = "Get a ward based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = WardResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "404",
                description = "Ward not found.",
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
    public static final class GetWardByIdService {

        private final GetWardByIdDatasource datasource;

        public WardResponse get(Long id) throws Exception {
            Optional<Ward> wardOptional = datasource.findById(id);
            if (wardOptional.isEmpty()) {
                throw new Exception("Ward not found");
            }
            Ward ward = wardOptional.get();

            return WardResponse.builder()
                    .id(ward.getId())
                    .name(ward.getName())
                    .district(ward.getDistrict().getName())
                    .cityProvince(ward.getDistrict().getCityProvince().getName())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetWardByIdDatasource {

        private final WardRepository wardRepository;

        public Optional<Ward> findById(Long id) {
            return wardRepository.findById(id);
        }
    }
}
