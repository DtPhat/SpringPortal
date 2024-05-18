package com.swd.uniportal.application.address.city_province;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.CityProvince;
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
public class GetCityProvinceById {

    @Builder
    public record CityProvinceResponse(Long id, String name, List<DistrictResponse> districts) {
    }

    @Builder
    public record DistrictResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class GetCityProvinceByIdController extends BaseController {

        private final GetCityProvinceByIdService service;

        @GetMapping("/addresses/wards/districts/city-provinces/{id}")
        @Operation(summary = "Get a city province based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CityProvinceResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "404",
                description = "City province not found.",
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
    public static final class GetCityProvinceByIdService {

        private final GetCityProvinceByIdDatasource datasource;

        public CityProvinceResponse get(Long id) throws Exception {
            Optional<CityProvince> cityProvinceOptional = datasource.findById(id);
            if (cityProvinceOptional.isEmpty()) {
                throw new Exception("City province not found");
            }
            CityProvince cityProvince = cityProvinceOptional.get();

            List<DistrictResponse> districtResponses = cityProvince.getDistricts().stream()
                    .map(district -> new DistrictResponse(district.getId(), district.getName()))
                    .collect(Collectors.toList());

            return new CityProvinceResponse(cityProvince.getId(), cityProvince.getName(), districtResponses);
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetCityProvinceByIdDatasource {

        private final CityProvinceRepository cityProvinceRepository;
        private final DistrictRepository districtRepository;

        public Optional<CityProvince> findById(Long id) {
            return cityProvinceRepository.findByIdWithDistricts(id);
        }
    }
}
