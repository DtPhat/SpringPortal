package com.swd.uniportal.application.address.district;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.District;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.DistrictRepository;
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
public class DeleteDistrictById {

    @Builder
    public record DeletedDistrictResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class DeleteDistrictController extends BaseController {

        private final DeleteDistrictService service;

        @DeleteMapping("/addresses/wards/districts/{id}")
        @Operation(summary = "Delete a district by ID.")
        @ApiResponse(responseCode = "200", description = "Successfully deleted.")
        @ApiResponse(responseCode = "404", description = "District not found.", content = {})
        @ApiResponse(responseCode = "500", description = "Server error.", content = {})
        public ResponseEntity<Object> deleteDistrictById(@PathVariable("id") Long id) {
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
    public static class DeleteDistrictService {

        private final DeleteDistrictDatasource datasource;

        public DeletedDistrictResponse deleteById(Long id) throws Exception {
            Optional<District> districtOptional = datasource.findById(id);
            if (districtOptional.isEmpty()) {
                throw new Exception("District not found");
            }
            District district = districtOptional.get();

            datasource.deleteDistrict(district);

            return new DeletedDistrictResponse(district.getId(), district.getName());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteDistrictDatasource {

        private final DistrictRepository districtRepository;

        public void deleteDistrict(District district) {
            districtRepository.delete(district);
        }

        public Optional<District> findById(Long id) {
            return districtRepository.findById(id);
        }
    }
}
