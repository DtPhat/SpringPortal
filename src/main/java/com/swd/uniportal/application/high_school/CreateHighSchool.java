package com.swd.uniportal.application.high_school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.high_school.exception.CityProvinceNotFoundException;
import com.swd.uniportal.application.high_school.exception.DuplicatedHighSchoolException;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.domain.institution.HighSchool;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import com.swd.uniportal.infrastructure.repository.HighSchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateHighSchool {

    @Data
    public static class CreateHighSchoolRequest {
        @NotBlank(message = "name: must not be null or blank.")
        private String name;

        private String description;

        @NotNull(message = "City province id must be defined.")
        @Min(value = 1, message = "City province id must be positive")
        private Long cityProvinceId;
    }

    @Builder
    public record CreatedHighSchoolResponse(
            Long id,
            String name,
            String description
    ) {
    }

    @RestController
    @Tag(name = "high-schools")
    @AllArgsConstructor
    public static final class CreateHighSchoolController extends BaseController {

        private final CreateHighSchoolService service;

        @PostMapping("/high-schools")
        @Operation(summary = "Create a high school.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "info to create.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreateHighSchoolRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreatedHighSchoolResponse.class)
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
        public ResponseEntity<Object> createHighSchool(@RequestBody CreateHighSchoolRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                CreatedHighSchoolResponse response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (DuplicatedHighSchoolException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class CreateHighSchoolService {

        private final CreateHighSchoolDatasource datasource;

        public CreatedHighSchoolResponse create(CreateHighSchoolRequest request)
                throws DuplicatedHighSchoolException, CityProvinceNotFoundException {
            CityProvince cityProvince = datasource.getCityProvince(request.getCityProvinceId())
                    .orElseThrow(() -> new CityProvinceNotFoundException(String
                            .format("City province with id '%d' not found", request.getCityProvinceId())));
            if (datasource.highSchoolIsDuplicated(request.getName(), request.getCityProvinceId())) {
                throw new DuplicatedHighSchoolException("Duplicated high school with same name and city province.");
            }

            HighSchool highSchool = HighSchool.builder()
                    .name(request.name)
                    .description(request.description)
                    .cityProvince(cityProvince)
                    .build();

            highSchool = datasource.persist(highSchool);

            return CreatedHighSchoolResponse.builder()
                    .id(highSchool.getId())
                    .description(highSchool.getDescription())
                    .name(highSchool.getName())
                    .build();
        }

    }

    @Datasource
    @AllArgsConstructor
    public static final class CreateHighSchoolDatasource {

        private final HighSchoolRepository highSchoolRepository;
        private final CityProvinceRepository cityProvinceRepository;

        public HighSchool persist(HighSchool highSchool) {
            return highSchoolRepository.save(highSchool);
        }

        public boolean highSchoolIsDuplicated(String searchName, Long cityProvinceId) {
            return highSchoolRepository.existsByNameIgnoreCaseAndCityProvinceId(searchName, cityProvinceId);
        }

        public Optional<CityProvince> getCityProvince(Long cityProvinceId) {
            return cityProvinceRepository.findById(cityProvinceId);
        }
    }
}
