package com.swd.uniportal.application.major.school;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.School;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.SchoolRepository;
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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateSchoolById {

    @Data
    public static class UpdateSchoolRequest {

        private String name;

        private String code;

        private String description;
    }

    @Builder
    public record SchoolUpdatedResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @AllArgsConstructor
    @Tag(name = "majors")
    public static class UpdateSchoolController extends BaseController {

        private final UpdateSchoolService service;

        @PutMapping("/majors/departments/schools/{id}")
        @Operation(summary = "Update a school by ID.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "School info to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdateSchoolRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successfully updated.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SchoolUpdatedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or School not found.",
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
        public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody UpdateSchoolRequest request) {
            try {
                SchoolUpdatedResponse response = service.update(id, request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }

    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class UpdateSchoolService {

        private final UpdateSchoolDatasource datasource;

        public SchoolUpdatedResponse update(Long id, UpdateSchoolRequest request) throws Exception {
            Optional<School> optionalSchool = datasource.findById(id);
            if (optionalSchool.isEmpty()) {
                throw new Exception("School not found");
            }
            School school = optionalSchool.get();

            if (StringUtils.isNotBlank(request.getName())) {
                school.setName(StringUtils.trim(request.getName()));
            }
            if (StringUtils.isNotBlank(request.getCode())) {
                school.setCode(StringUtils.trim(request.getCode()));
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                school.setDescription(StringUtils.trim(request.getDescription()));
            }

            school = datasource.update(school);

            return new SchoolUpdatedResponse(
                    school.getId(), school.getName(), school.getCode(), school.getDescription());
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateSchoolDatasource {

        private final SchoolRepository schoolRepository;

        public Optional<School> findById(Long id) {
            return schoolRepository.findById(id);
        }

        public School update(School school) {
            return schoolRepository.save(school);
        }
    }
}
