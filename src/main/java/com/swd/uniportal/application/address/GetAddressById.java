package com.swd.uniportal.application.address;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.Address;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AddressRepository;
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
public class GetAddressById {

    @Builder
    public record AddressResponse(long id, String houseNumber, String streetName, String ward, String district,
                                  String cityProvince) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class GetAddressByIdController extends BaseController {

        private final GetAddressByIdService service;

        @GetMapping("/addresses/{id}")
        @Operation(summary = "Get an address based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AddressResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "404",
                description = "Address not found.",
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
    public static final class GetAddressByIdService {

        private final GetAddressByIdDatasource datasource;

        public AddressResponse get(Long id) throws Exception {
            Optional<Address> addressOptional = datasource.findById(id);
            if (addressOptional.isEmpty()) {
                throw new Exception("Address not found");
            }
            Address address = addressOptional.get();

            return AddressResponse.builder()
                    .id(address.getId())
                    .houseNumber(address.getHouseNumber())
                    .streetName(address.getStreetName())
                    .ward(address.getWard().getName())
                    .district(address.getDistrict().getName())
                    .cityProvince(address.getCityProvince().getName())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetAddressByIdDatasource {

        private final AddressRepository addressRepository;

        public Optional<Address> findById(Long id) {
            return addressRepository.findById(id);
        }

    }

}
