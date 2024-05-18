package com.swd.uniportal.application.institution;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.institution.exception.InstitutionNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.address.Address;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.domain.institution.Email;
import com.swd.uniportal.domain.institution.Institution;
import com.swd.uniportal.domain.institution.Phone;
import com.swd.uniportal.domain.institution.Website;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.AddressRepository;
import com.swd.uniportal.infrastructure.repository.InstitutionRepository;
import com.swd.uniportal.infrastructure.repository.WardRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateInstitutionById {

    @Data
    public static class UpdateInstitutionRequest {

        @NotBlank(message = "name: must not be null or blank.")
        private String name;

        @NotBlank(message = "code: must not be null or blank.")
        private String code;

        private String description;
        private String avatarLink;
        private Website website1;
        private Website website2;
        private Website website3;
        private Email email1;
        private Email email2;
        private Email email3;
        private Phone phone1;
        private Phone phone2;
        private Phone phone3;

        @NotEmpty(message = "addresses array must not be empty.")
        @Valid
        private List<AddressRequest> addresses = new ArrayList<>();

    }

    @Data
    public static class AddressRequest {

        @NotBlank(message = "houseNumber: must not be null or blank.")
        private String houseNumber;

        @NotBlank(message = "streetName: must not be null or blank.")
        private String streetName;

        @NotNull(message = "wardId: must not be null.")
        private Long wardId;

        private String description;
    }

    @Builder
    public record InstitutionUpdatedResponse(
            Long id,
            String name,
            String code,
            String description,
            String avatarLink,
            List<Website> websites,
            List<Email> emails, List<Phone> phones,
            List<AddressResponse> addresses
    ) {
    }

    @Builder
    public record AddressResponse(Long id, String houseNumber, String streetName, String ward, String district,
                                  String cityProvince) {
    }

    @RestController
    @AllArgsConstructor
    @Tag(name = "institutions")
    public static class UpdateInstitutionController extends BaseController {

        private final UpdateInstitutionByIdService service;

        @PutMapping("/institutions/{id}")
        @Operation(summary = "Update an institution by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Institution info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateInstitutionRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = InstitutionUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or Institution not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateInstitutionRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                InstitutionUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (InstitutionNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateInstitutionByIdService {

        private final UpdateInstitutionByIdDatasource datasource;

        public InstitutionUpdatedResponse update(Long id, UpdateInstitutionRequest request) throws InstitutionNotFoundException, AccountNotFoundException {
            Institution institution = datasource.getInstitution(id);

            if (institution == null) {
                throw new InstitutionNotFoundException("Institution not found");
            }

            institution.setName(request.name);
            institution.setCode(request.code);
            institution.setDescription(request.description);
            institution.setAvatarLink(request.avatarLink);
            institution.setWebsite1(request.website1);
            institution.setWebsite2(request.website2);
            institution.setWebsite3(request.website3);
            institution.setEmail1(request.email1);
            institution.setEmail2(request.email2);
            institution.setEmail3(request.email3);
            institution.setPhone1(request.phone1);
            institution.setPhone2(request.phone2);
            institution.setPhone3(request.phone3);

            institution.getAddresses().clear();

            List<Address> addresses = new ArrayList<>();
            for (AddressRequest addressRequest : request.getAddresses()) {
                Ward ward = datasource.getWardById(addressRequest.getWardId())
                        .orElseThrow(() -> new IllegalArgumentException("Ward not found"));
                Address address = Address.builder()
                        .houseNumber(addressRequest.getHouseNumber())
                        .streetName(addressRequest.getStreetName())
                        .ward(ward)
                        .district(ward.getDistrict())
                        .cityProvince(ward.getDistrict().getCityProvince())
                        .description(addressRequest.getDescription())
                        .institution(institution)
                        .build();
                addresses.add(address);
            }

            institution.getAddresses().addAll(addresses);

            institution = datasource.update(institution);

            return InstitutionUpdatedResponse.builder()
                    .id(institution.getId())
                    .name(institution.getName())
                    .code(institution.getCode())
                    .description(institution.getDescription())
                    .avatarLink(institution.getAvatarLink())
                    .addresses(institution.getAddresses().stream().map(address -> AddressResponse.builder()
                            .id(address.getId())
                            .streetName(address.getStreetName())
                            .houseNumber(address.getHouseNumber())
                            .ward(address.getWard().getName())
                            .district(address.getDistrict().getName())
                            .cityProvince(address.getCityProvince().getName())
                            .build()).toList())
                    .websites(Stream.of(institution.getWebsite1(), institution.getWebsite2(), institution.getWebsite3()).filter(Objects::nonNull).toList())
                    .emails(Stream.of(institution.getEmail1(), institution.getEmail2(), institution.getEmail3()).filter(Objects::nonNull).toList())
                    .phones(Stream.of(institution.getPhone1(), institution.getPhone2(), institution.getPhone3()).filter(Objects::nonNull).toList())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateInstitutionByIdDatasource {

        private final InstitutionRepository institutionRepository;
        private final WardRepository wardRepository;
        private final AccountRepository accountRepository;
        private final AddressRepository addressRepository;

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public Optional<Ward> getWardById(Long id) {
            return wardRepository.findById(id);
        }

        public Institution getInstitution(Long id) {
            return institutionRepository.findByIdWithAddresses(id);
        }

        public Institution update(Institution institution) {
            return institutionRepository.save(institution);
        }
    }
}
