package com.swd.uniportal.application.address.city_province;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.CityProvince;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.CityProvinceRepository;
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
public class DeleteCityProvinceById {

    @Builder
    public record DeletedCityProvinceResponse(Long id, String name) {
    }

    @RestController
    @Tag(name = "addresses")
    @AllArgsConstructor
    public static final class DeleteCityProvinceController extends BaseController {

        private final DeleteCityProvinceService service;

        @DeleteMapping("/addresses/wards/districts/city-provinces/{id}")
        @Operation(summary = "Delete a city or province by ID.")
        @ApiResponse(responseCode = "200", description = "Successfully deleted.")
        @ApiResponse(responseCode = "404", description = "City or province not found.", content = {})
        @ApiResponse(responseCode = "500", description = "Server error.", content = {})
        public ResponseEntity<Object> deleteCityProvinceById(@PathVariable("id") Long id) {
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
    public static class DeleteCityProvinceService {

        private final DeleteCityProvinceDatasource datasource;

        public DeletedCityProvinceResponse deleteById(Long id) throws Exception {
            Optional<CityProvince> cityProvinceOptional = datasource.findById(id);
            if (cityProvinceOptional.isEmpty()) {
                throw new Exception("City or province not found");
            }
            CityProvince cityProvince = cityProvinceOptional.get();

            datasource.deleteCityProvince(cityProvince);

            return new DeletedCityProvinceResponse(cityProvince.getId(), cityProvince.getName());
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteCityProvinceDatasource {

        private final CityProvinceRepository cityProvinceRepository;

        public void deleteCityProvince(CityProvince cityProvince) {
            cityProvinceRepository.delete(cityProvince);
        }

        public Optional<CityProvince> findById(Long id) {
            return cityProvinceRepository.findById(id);
        }
    }
}
