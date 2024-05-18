package com.swd.uniportal.application.major;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.major.Major;
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
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteMajorById {

    @Builder
    public record DeletedMajorResponse(Long id, String name, String code, String description) {
    }

    @RestController
    @Tag(name = "majors")
    @AllArgsConstructor
    public static final class DeleteMajorController extends BaseController {

        private final DeleteMajorService service;

        @DeleteMapping("/majors/{id}")
        @Operation(summary = "Delete a major by ID.")
        @ApiResponse(responseCode = "200", description = "Successfully deleted.")
        @ApiResponse(responseCode = "404", description = "Major not found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        @ApiResponse(responseCode = "500", description = "Server error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedResponse.class)))
        public ResponseEntity<Object> deleteMajorById(@PathVariable("id") Long id) {
            try {
                return ResponseEntity.ok(service.deleteById(id));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class DeleteMajorService {

        private final DeleteMajorDatasource datasource;

        public DeletedMajorResponse deleteById(Long id) throws Exception {
            Optional<Major> majorOptional = datasource.findById(id);
            if (majorOptional.isEmpty()) {
                throw new Exception("Major not found");
            }
            Major major = majorOptional.get();

            datasource.deleteMajor(major);

            return new DeletedMajorResponse(major.getId(), major.getName(), major.getCode(), major.getDescription());
        }

    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteMajorDatasource {

        private final MajorRepository majorRepository;

        public void deleteMajor(Major major) {
            majorRepository.delete(major);
        }

        public Optional<Major> findById(Long id) {
            return majorRepository.findById(id);
        }
    }
}