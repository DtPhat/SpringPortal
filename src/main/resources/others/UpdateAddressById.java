//package com.swd.uniportal.application.address;
//
//import com.swd.uniportal.application.common.BaseController;
//import com.swd.uniportal.application.common.FailedResponse;
//import com.swd.uniportal.domain.address.Address;
//import com.swd.uniportal.domain.address.Ward;
//import com.swd.uniportal.infrastructure.annotation.Datasource;
//import com.swd.uniportal.infrastructure.repository.AddressRepository;
//import com.swd.uniportal.infrastructure.repository.InstitutionRepository;
//import com.swd.uniportal.infrastructure.repository.WardRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Optional;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//public class UpdateAddressById {
//
//    @Data
//    public static class UpdateAddressRequest {
//
//        private String houseNumber;
//
//        private String streetName;
//
//        private Long wardId;
//
//        private String description;
//
//    }
//
//    @Builder
//    public record AddressUpdatedResponse(Long id, String houseNumber, String streetName, Long wardId) {}
//
//    @RestController
//    @Tag(name = "addresses")
//    @AllArgsConstructor
//    public static final class UpdateAddressController extends BaseController {
//
//        private final UpdateAddressService service;
//
//        @PutMapping("/addresses/{id}")
//        @Operation(summary = "Update an address by ID.")
//        @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                description = "Address info to update.",
//                content = @Content(
//                        mediaType = "application/json",
//                        schema = @Schema(implementation = UpdateAddressRequest.class)
//                )
//        )
//        @ApiResponse(
//                responseCode = "200",
//                description = "Successfully updated.",
//                content = @Content(
//                        mediaType = "application/json",
//                        schema = @Schema(implementation = AddressUpdatedResponse.class)
//                )
//        )
//        @ApiResponse(
//                responseCode = "400",
//                description = "Invalid request body or Address not found.",
//                content = @Content(
//                        mediaType = "application/json",
//                        schema = @Schema(implementation = FailedResponse.class)
//                )
//        )
//        @ApiResponse(
//                responseCode = "500",
//                description = "Server error.",
//                content = @Content(
//                        mediaType = "application/json",
//                        schema = @Schema(implementation = FailedResponse.class)
//                )
//        )
//        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateAddressRequest request) {
//            try {
//                AddressUpdatedResponse response = service.update(id, request);
//                return ResponseEntity.ok(response);
//            } catch (Exception e) {
//                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
//            }
//        }
//    }
//
//    @Service
//    @AllArgsConstructor
//    @Transactional
//    public static class UpdateAddressService {
//
//        private final UpdateAddressDatasource datasource;
//
//        public AddressUpdatedResponse update(Long id, UpdateAddressRequest request) throws Exception {
//            Optional<Address> addressOptional = datasource.findById(id);
//            if (addressOptional.isEmpty()) {
//                throw new Exception("Address not found");
//            }
//            Address address = addressOptional.get();
//
//            address.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
//
//            if (StringUtils.isNotBlank(request.getHouseNumber())) {
//                address.setHouseNumber(StringUtils.trim(request.getHouseNumber()));
//            }
//
//            if (StringUtils.isNotBlank(request.getStreetName())) {
//                address.setStreetName(StringUtils.trim(request.getStreetName()));
//            }
//
//            if (request.getWardId() != null) {
//                Ward ward = datasource.getWardById(request.getWardId()).orElseThrow(() -> new IllegalArgumentException("Ward not found"));
//                address.setWard(ward);
//                address.setDistrict(ward.getDistrict());
//                address.setCityProvince(ward.getDistrict().getCityProvince());
//            }
//
//            if (StringUtils.isNotBlank(request.getDescription())) {
//                address.setDescription(request.getDescription());
//            }
//
//            Address updatedAddress = datasource.update(address);
//
//            return new AddressUpdatedResponse(
//                    updatedAddress.getId(),
//                    updatedAddress.getHouseNumber(),
//                    updatedAddress.getStreetName(),
//                    updatedAddress.getWard().getId()
//            );
//        }
//    }
//
//    @Datasource
//    @AllArgsConstructor
//    public static class UpdateAddressDatasource {
//
//        private final AddressRepository addressRepository;
//        private final WardRepository wardRepository;
//        private final InstitutionRepository institutionRepository;
//
//        public Optional<Address> findById(Long id) {
//            return addressRepository.findById(id);
//        }
//
//        public Optional<Ward> getWardById(Long id) {
//            return wardRepository.findById(id);
//        }
//
//        public Address update(Address address) {
//            return addressRepository.save(address);
//        }
//    }
//}
