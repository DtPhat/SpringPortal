package com.swd.uniportal.application.major;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.major.dto.DepartmentDto;
import com.swd.uniportal.application.major.dto.MajorDto;
import com.swd.uniportal.application.major.dto.SchoolDto;
import com.swd.uniportal.domain.major.Department;
import com.swd.uniportal.domain.major.Major;
import com.swd.uniportal.domain.major.School;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.MajorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetMajorById {

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class GetMajorByIdController extends BaseController {

        private final GetMajorByIdService service;

        @GetMapping("/majors/{id}")
        @Operation(summary = "Get a major based on its ID.")
        @ApiResponse(responseCode = "200", description = "Founded.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MajorDto.class)))
        @ApiResponse(responseCode = "400", description = "Invalid parameters.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        @ApiResponse(responseCode = "403", description = "No permission to get major with current role.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        @ApiResponse(responseCode = "500", description = "Server error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
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
    public static final class GetMajorByIdService {

        private final GetMajorByIdDatasource datasource;

        public MajorDto get(Long id) throws Exception {
            Optional<Major> majorOptional = datasource.findById(id);
            if (majorOptional.isEmpty()) {
                throw new Exception("Major not found");
            }
            Major major = majorOptional.get();
            Department department = major.getDepartment();
            School school = department.getSchool();
            return MajorDto.builder()
                    .id(major.getId())
                    .code(major.getCode())
                    .name(major.getName())
                    .description(major.getDescription())
                    .department(DepartmentDto.builder()
                            .id(department.getId())
                            .code(department.getCode())
                            .name(department.getName())
                            .build())
                    .school(SchoolDto.builder()
                            .id(school.getId())
                            .code(school.getCode())
                            .name(school.getName())
                            .build())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static final class GetMajorByIdDatasource {

        private final MajorRepository majorRepository;

        public Optional<Major> findById(Long id) {
            return majorRepository.findByIdPopulated(id);
        }
    }
}
