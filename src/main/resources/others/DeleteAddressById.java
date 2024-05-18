//package com.swd.uniportal.application.address;
//
//import com.swd.uniportal.application.common.BaseController;
//import com.swd.uniportal.application.common.FailedResponse;
//import com.swd.uniportal.infrastructure.annotation.Datasource;
//import com.swd.uniportal.infrastructure.repository.AddressRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import java.util.List;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//public class DeleteAddressById {
//
//    @RestController
//    @Tag(name = "addresses")
//    @AllArgsConstructor
//    public static final class DeleteAddressController extends BaseController {
//
//        private final DeleteAddressService service;
//
//        @DeleteMapping("/addresses/{id}")
//        @Operation(summary = "Delete an address by ID.")
//        @ApiResponse(
//                responseCode = "200",
//                description = "Successfully deleted."
//        )
//        @ApiResponse(
//                responseCode = "404",
//                description = "Address not found.",
//                content = @Content(
//                        mediaType = "application/json",
//                        schema = @Schema(implementation = FailedResponse.class)
//                )
//        )
//        @ApiResponse(
//                responseCode = "500",
//                description = "Server error.",
//                content = @Content(
//                        mediaType = "application/json",
//                        schema = @Schema(implementation = FailedResponse.class)
//                )
//        )
//        public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
//            try {
//                service.delete(id);
//                return ResponseEntity.ok().build();
//            } catch (Exception e) {
//                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
//            }
//        }
//    }
//
//    @Service
//    @AllArgsConstructor
//    @Transactional
//    public static class DeleteAddressService {
//
//        private final DeleteAddressDatasource datasource;
//
//        public void delete(Long id) throws Exception {
//            datasource.delete(id);
//        }
//    }
//
//    @Datasource
//    @AllArgsConstructor
//    public static class DeleteAddressDatasource {
//
//        private final AddressRepository addressRepository;
//        private final InstitutionAddressRepository institutionAddressRepository;
//
//        public void delete(Long id) throws Exception {
//            addressRepository.deleteById(id);
//            institutionAddressRepository.deleteByAddressId(id);
//        }
//    }
//}
