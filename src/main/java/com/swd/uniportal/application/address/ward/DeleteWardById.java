package com.swd.uniportal.application.address.ward;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.Ward;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.WardRepository;
import io.swagger.v3.oas.annotations.Operation;
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
public class DeleteWardById {

    @Builder
    public record DeletedWardResponse(Long id, String name) {}

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class DeleteWardController extends BaseController {

        private final DeleteWardService service;

        @DeleteMapping("/addresses/wards/{id}")
        @Operation(summary = "Delete a ward by ID.")
        @ApiResponse(responseCode = "200", description = "Successfully deleted.")
        @ApiResponse(responseCode = "404", description = "Ward not found.", content = {})
        @ApiResponse(responseCode = "500", description = "Server error.", content = {})
        public ResponseEntity<Object> deleteWardById(@PathVariable("id") Long id) {
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
    public static class DeleteWardService {

        private final DeleteWardDatasource datasource;

        public DeletedWardResponse deleteById(Long id) throws Exception {
            Optional<Ward> wardOptional = datasource.findById(id);
            if (wardOptional.isEmpty()) {
                throw new Exception("Ward not found");
            }
            Ward ward = wardOptional.get();

            datasource.deleteWard(ward);

            return new DeletedWardResponse(ward.getId(), ward.getName());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteWardDatasource {

        private final WardRepository wardRepository;

        public void deleteWard(Ward ward) {
            wardRepository.delete(ward);
        }

        public Optional<Ward> findById(Long id) {
            return wardRepository.findById(id);
        }
    }
}
