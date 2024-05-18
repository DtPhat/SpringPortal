package com.swd.uniportal.application.high_school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.high_school.exception.CityProvinceNotFoundException;
import com.swd.uniportal.application.high_school.exception.DuplicatedHighSchoolException;
import com.swd.uniportal.application.high_school.exception.HighSchoolNotFoundException;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.domain.institution.HighSchool;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
import com.swd.uniportal.infrastructure.repository.HighSchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateHighSchool {

    @Data
    @Builder
    public static final class UpdateHighSchoolRequest {

        @NotBlank(message = "name: must not be null or blank.")
        private String name;

        private String description;

        @NotNull(message = "City province id must be defined.")
        @Min(value = 1, message = "City province id must be positive")
        private Long cityProvinceId;
    }

    @Builder
    public record UpdatedHighSchoolResponse(
            Long id,
            String name,
            String description,
            Long cityProvinceId
    ) {
    }

    @RestController
    @Tag(name = "high-schools")
    @AllArgsConstructor
    public static final class UpdateHighSchoolController extends BaseController {

        private final UpdateHighSchoolService service;

        @PutMapping("/high-schools/{id}")
        @Operation(summary = "Update high school by id.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "high school to update.",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(
                                items = @Schema(implementation = UpdateHighSchoolRequest.class)
                        )
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdatedHighSchoolResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request.",
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
        public ResponseEntity<Object> updateHighSchoolById(
                @PathVariable Long id,
                @RequestBody UpdateHighSchoolRequest request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            request.setName(StringUtils.trim(request.getName()));
            request.setDescription(StringUtils.trim(request.getDescription()));
            try {
                UpdatedHighSchoolResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (DuplicatedHighSchoolException
                     | HighSchoolNotFoundException
                     | CityProvinceNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class UpdateHighSchoolService {

        private final UpdateHighSchoolDatasource datasource;

        public UpdatedHighSchoolResponse update(Long id, UpdateHighSchoolRequest request)
                throws HighSchoolNotFoundException,
                DuplicatedHighSchoolException,
                CityProvinceNotFoundException {
            HighSchool highSchool = datasource.get(id);
            if (datasource.highSchoolIsDuplicated(request.getName(), request.getCityProvinceId())) {
                throw new DuplicatedHighSchoolException("Duplicated high school with same name and city province.");
            }
            CityProvince cityProvince = datasource.getCityProvince(request.getCityProvinceId())
                    .orElseThrow(() -> new CityProvinceNotFoundException(String
                            .format("City province with id '%d' not found", request.getCityProvinceId())));

            highSchool.setName(request.getName());
            highSchool.setDescription(request.getDescription());
            highSchool.setCityProvince(cityProvince);
            highSchool = datasource.update(highSchool);

            return UpdatedHighSchoolResponse.builder()
                    .id(highSchool.getId())
                    .name(highSchool.getName())
                    .description(highSchool.getDescription())
                    .build();
        }

    }

    @Datasource
    @AllArgsConstructor
    public static final class UpdateHighSchoolDatasource {

        private final HighSchoolRepository highSchoolRepository;
        private final CityProvinceRepository cityProvinceRepository;

        public HighSchool get(Long id) throws HighSchoolNotFoundException {
            return highSchoolRepository.getByIdPopulated(id).
                    orElseThrow(() -> new HighSchoolNotFoundException("High School not found"));
        }

        public HighSchool update(HighSchool highschool) {
            return highSchoolRepository.save(highschool);
        }

        public boolean highSchoolIsDuplicated(String searchName, Long cityProvinceId) {
            return highSchoolRepository.existsByNameIgnoreCaseAndCityProvinceId(searchName, cityProvinceId);
        }

        public Optional<CityProvince> getCityProvince(Long cityProvinceId) {
            return cityProvinceRepository.findById(cityProvinceId);
        }
    }
}
