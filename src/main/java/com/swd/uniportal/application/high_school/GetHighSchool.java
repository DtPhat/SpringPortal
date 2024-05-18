package com.swd.uniportal.application.high_school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.high_school.exception.HighSchoolNotFoundException;
import com.swd.uniportal.domain.institution.HighSchool;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.HighSchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
public class GetHighSchool {

    @Builder
    public record HighSchoolResponse(Long id, String name, String description, Long cityProvinceId) {
    }

    @RestController
    @Tag(name = "high-schools")
    @AllArgsConstructor
    public static final class GetHighSchoolController extends BaseController {

        private final GetHighSchoolService service;

        @Operation(summary = "Get high school based on its ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = HighSchoolResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "404",
                description = "High school not found.",
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
        @GetMapping("/high-schools/{id}")
        public ResponseEntity<Object> getHighSchoolById(@PathVariable("id") Long id) {
             try {
                return ResponseEntity.ok(service.get(id));
            } catch (HighSchoolNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    public static final class GetHighSchoolService {

        private final GetHighSchoolDataSource datasource;
        public HighSchoolResponse get(Long id) throws HighSchoolNotFoundException {
            HighSchool highSchool = datasource.get(id);
            return HighSchoolResponse.builder()
                    .id(highSchool.getId())
                    .name(highSchool.getName())
                    .description(highSchool.getDescription())
                    .cityProvinceId(highSchool.getCityProvince().getId())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetHighSchoolDataSource {

        private final HighSchoolRepository highSchoolRepository;

        public HighSchool get(Long id) throws HighSchoolNotFoundException {
            return highSchoolRepository.findById(id).orElseThrow(() -> new HighSchoolNotFoundException("High school not found"));
        }
    }
}
