//package com.swd.uniportal.application.address;
//
//import com.swd.uniportal.application.common.BaseController;
//import com.swd.uniportal.application.common.CustomValidation;
//import com.swd.uniportal.application.common.FailedResponse;
//import com.swd.uniportal.application.common.exception.AccountNotFoundException;
//import com.swd.uniportal.domain.account.Account;
//import com.swd.uniportal.domain.address.Address;
//import com.swd.uniportal.domain.address.Ward;
//import com.swd.uniportal.domain.institution.Institution;
//import com.swd.uniportal.infrastructure.annotation.Datasource;
//import com.swd.uniportal.infrastructure.repository.AccountRepository;
//import com.swd.uniportal.infrastructure.repository.AddressRepository;
//import com.swd.uniportal.infrastructure.repository.InstitutionRepository;
//import com.swd.uniportal.infrastructure.repository.WardRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.constraints.NotNull;
//import java.net.URI;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Optional;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//public class CreateAddress {
//
//    @Data
//    public static class CreateAddressRequest {
//
//        private String houseNumber;
//
//        private String streetName;
//
//        @NotNull(message = "wardId must not be null.")
//        private Long wardId;
//
//        @NotNull(message = "institutionId must not be null.")
//        private Long institutionId;
//
//        private String description;
//    }
//
//    @Builder
//    public record AddressCreatedResponse(Long id, String houseNumber, String streetName, Long wardId, Long institutionId) {
//    }
//
//    @RestController
//    @Tag(name = "addresses")
//    @AllArgsConstructor
//    public static final class CreateAddressController extends BaseController {
//
//        private final CreateAddressService service;
//
//        @PostMapping("/addresses")
//        @Operation(summary = "Create an address.")
//        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Address info to create.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateAddressRequest.class)))
//        @ApiResponse(responseCode = "201", description = "Successfully created.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressCreatedResponse.class)))
//        @ApiResponse(responseCode = "400", description = "Invalid request body.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
//        @ApiResponse(responseCode = "500", description = "Server error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
//        public ResponseEntity<Object> create(@RequestBody CreateAddressRequest request) {
//            List<String> violations = CustomValidation.validate(request);
//            if (!violations.isEmpty()) {
//                return ResponseEntity.badRequest().body(new FailedResponse(violations));
//            }
//            try {
//                AddressCreatedResponse response = service.create(request);
//                return ResponseEntity.created(new URI("")).body(response);
//            } catch (Exception e) {
//                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
//            }
//        }
//    }
//
//    @Service
//    @AllArgsConstructor
//    @Transactional
//    public static class CreateAddressService {
//
//        private final CreateAddressDatasource datasource;
//
//        public AddressCreatedResponse create(CreateAddressRequest request) throws AccountNotFoundException {
//            Ward ward = datasource.getWardById(request.getWardId()).orElseThrow(() -> new IllegalArgumentException("Ward not found"));
//
//            Institution institution = datasource.getInstitutionById(request.getInstitutionId()).orElseThrow(() -> new IllegalArgumentException("Institution not found"));
//
//            Account account = datasource.getAccountById(1L).orElseThrow(() -> new AccountNotFoundException(String.format("Account with id '%d' not found.", 1)));
//
//            Address address = Address.builder()
//                    .houseNumber(request.getHouseNumber())
//                    .streetName(request.getStreetName())
//                    .ward(ward)
//                    .district(ward.getDistrict())
//                    .cityProvince(ward.getDistrict().getCityProvince())
//                    .description(request.getDescription())
//                    .createdBy(account)
//                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
//                    .build();
//
//            Address createdAddress = datasource.persist(address);
//
//            InstitutionAddress institutionAddress = InstitutionAddress.builder()
//                    .institution(institution)
//                    .address(address)
//                    .createdBy(account)
//                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
//                    .build();
//
//            datasource.createInstitutionAddressAssociation(institutionAddress);
//
//            return new AddressCreatedResponse(
//                    createdAddress.getId(),
//                    createdAddress.getHouseNumber(),
//                    createdAddress.getStreetName(),
//                    ward.getId(),
//                    institution.getId()
//            );
//        }
//    }
//
//    @Datasource
//    @AllArgsConstructor
//    public static class CreateAddressDatasource {
//
//        private final AddressRepository addressRepository;
//        private final WardRepository wardRepository;
//        private final InstitutionRepository institutionRepository;
//        private final InstitutionAddressRepository institutionAddressRepository;
//        private final AccountRepository accountRepository;
//
//        public Optional<Account> getAccountById(Long id) {
//            return accountRepository.findById(id);
//        }
//
//        public Optional<Ward> getWardById(Long id) {
//            return wardRepository.findById(id);
//        }
//
//        public Optional<Institution> getInstitutionById(Long id) {
//            return institutionRepository.findById(id);
//        }
//
//        public Address persist(Address address) {
//            return addressRepository.save(address);
//        }
//
//        public void createInstitutionAddressAssociation(InstitutionAddress institutionAddress) {
//            institutionAddressRepository.save(institutionAddress);
//        }
//    }
//}
