package com.swd.uniportal.application.institution;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.address.Address;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.domain.institution.Email;
import com.swd.uniportal.domain.institution.Institution;
import com.swd.uniportal.domain.institution.Phone;
import com.swd.uniportal.domain.institution.Website;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
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
import java.net.URI;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateInstitution {

    @Data
    public static class CreateInstitutionRequest {
        @NotBlank(message = "name: must not be null or blank.")
        private String name;

        @NotBlank(message = "code: must not be null or blank.")
        private String code;

        private String description;
        private Website website1;
        private Website website2;
        private Website website3;
        private Email email1;
        private Email email2;
        private Email email3;
        private Phone phone1;
        private Phone phone2;
        private Phone phone3;
        private String avatarLink;

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
    public record InstitutionCreatedResponse(
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
    public static class CreateInstitutionController extends BaseController {

        private final CreateInstitutionService service;

        @PostMapping("/institutions")
        @Operation(summary = "Create an institution.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Institution info to create.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreateInstitutionRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = InstitutionCreatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body.",
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
        public ResponseEntity<Object> create(@RequestBody CreateInstitutionRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                InstitutionCreatedResponse response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (AccountNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class CreateInstitutionService {

        private final CreateInstitutionDatasource datasource;

        public InstitutionCreatedResponse create(CreateInstitutionRequest request) throws AccountNotFoundException {
            Institution institution = Institution.builder()
                    .name(StringUtils.trim(request.getName()))
                    .code(StringUtils.trim(request.getCode()))
                    .description(StringUtils.trimToNull(request.getDescription()))
                    .avatarLink(StringUtils.trim(request.getAvatarLink()))
                    .website1(request.getWebsite1())
                    .website2(request.getWebsite2())
                    .website3(request.getWebsite3())
                    .email1(request.getEmail1())
                    .email2(request.getEmail2())
                    .email3(request.getEmail3())
                    .phone1(request.getPhone1())
                    .phone2(request.getPhone2())
                    .phone3(request.getPhone3())
                    .addresses(new ArrayList<>())
                    .build();


            request.getAddresses().forEach(addressRequest -> {
                var ward = datasource.getWardById(addressRequest.wardId).orElseThrow(() -> new IllegalArgumentException("Ward not found"));
                Address address = Address.builder()
                        .houseNumber(addressRequest.houseNumber)
                        .streetName(addressRequest.streetName)
                        .description(addressRequest.description)
                        .ward(ward)
                        .district(ward.getDistrict())
                        .cityProvince(ward.getDistrict().getCityProvince())
                        .build();
                institution.addAddress(address);
            });


            Institution createdInstitution = datasource.persist(institution);

            return InstitutionCreatedResponse.builder()
                    .id(createdInstitution.getId())
                    .name(createdInstitution.getName())
                    .code(createdInstitution.getCode())
                    .description(createdInstitution.getDescription())
                    .avatarLink(createdInstitution.getAvatarLink())
                    .addresses(createdInstitution.getAddresses().stream().map(address -> AddressResponse.builder()
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
    public static class CreateInstitutionDatasource {

        private final InstitutionRepository institutionRepository;
        private final AccountRepository accountRepository;
        private final WardRepository wardRepository;

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public Institution persist(Institution institution) {
            return institutionRepository.save(institution);
        }

        public Optional<Ward> getWardById(Long id) {
            return wardRepository.findById(id);
        }
    }
}