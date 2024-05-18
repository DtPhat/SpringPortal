package com.swd.uniportal.application.institution;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.institution.exception.InstitutionNotFoundException;
import com.swd.uniportal.domain.institution.Email;
import com.swd.uniportal.domain.institution.Institution;
import com.swd.uniportal.domain.institution.Phone;
import com.swd.uniportal.domain.institution.Website;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.InstitutionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetInstitutionById {

    @Builder
    public record AddressResponse(Long id, String houseNumber, String streetName, String ward, String district,
                                  String cityProvince) {
    }

    @Builder
    public record InstitutionResponse(Long id, String name, String code, String description, String avatarLink,
                                      List<Website> websites,
                                      List<Email> emails, List<Phone> phones, List<AddressResponse> addresses) {
    }

    @RestController
    @Tag(name = "institutions")
    @AllArgsConstructor
    public static final class GetInstitutionByIdController extends BaseController {

        private final GetInstitutionByIdService service;

        @GetMapping("/institutions/{id}")
        @Operation(summary = "Get an institution based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = InstitutionResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "404",
                description = "Institution not found.",
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
            } catch (InstitutionNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class GetInstitutionByIdService {

        private final GetInstitutionByIdDatasource datasource;

        public InstitutionResponse get(Long id) throws InstitutionNotFoundException {
            Institution institution = datasource.getInstitution(id);

            if (institution == null) {
                throw new InstitutionNotFoundException("Institution not found");
            }

            return InstitutionResponse.builder()
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
                    .emails(Stream.of(institution.getEmail1(),institution.getEmail2(),institution.getEmail3()).filter(Objects::nonNull).toList())
                    .phones(Stream.of(institution.getPhone1(),institution.getPhone2(),institution.getPhone3()).filter(Objects::nonNull).toList())
                    .websites(Stream.of(institution.getWebsite1(),institution.getWebsite2(),institution.getWebsite3()).filter(Objects::nonNull).toList())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetInstitutionByIdDatasource {

        private final InstitutionRepository institutionRepository;

        public Institution getInstitution(Long id) {
            return institutionRepository.findByIdWithAddresses(id);
        }

    }
}
