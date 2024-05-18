package com.swd.uniportal.application.admission.major_method;

import com.swd.uniportal.application.admission.exception.AdmissionMajorMethodNotFound;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionMajorMethodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteAdmissionMajorMethod {

    @RestController
    @Tag(name = "admission-plans")
    public static final class DeleteAdmissionMajorMethodController extends BaseController {

        private final DeleteAdmissionMajorMethodService service;

        @Autowired
        public DeleteAdmissionMajorMethodController(DeleteAdmissionMajorMethodService service) {
            this.service = service;
        }

        @DeleteMapping("/admission-plans/majors/methods/{admissionMajorMethodId}")
        @Operation(summary = "Delete admission major method.")
        @ApiResponse(
                responseCode = "204",
                description = "Successful."
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
        public ResponseEntity<Object> delete(
                @PathVariable("admissionMajorMethodId") Long admissionMajorMethodId
        ) {
            if (admissionMajorMethodId < 1) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of("Admission major id must be positive.")));
            }
            try {
                service.delete(admissionMajorMethodId);
                return ResponseEntity.noContent().build();
            } catch (AdmissionMajorMethodNotFound e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class DeleteAdmissionMajorMethodService {

        private final DeleteAdmissionMajorMethodDatasource datasource;

        @Autowired
        public DeleteAdmissionMajorMethodService(DeleteAdmissionMajorMethodDatasource datasource) {
            this.datasource = datasource;
        }

        public void delete(Long admissionMajorMethodId)
                throws AdmissionMajorMethodNotFound {
            if (datasource.admissionMajorMethodDoesNotExist(admissionMajorMethodId)) {
                throw new AdmissionMajorMethodNotFound(String
                        .format("Admission major method with id '%d' not found.", admissionMajorMethodId));
            }
            datasource.delete(admissionMajorMethodId);
        }
    }

    @Datasource
    public static final class DeleteAdmissionMajorMethodDatasource {

        private final AdmissionMajorMethodRepository admissionMajorMethodRepository;

        @Autowired
        public DeleteAdmissionMajorMethodDatasource(AdmissionMajorMethodRepository admissionMajorMethodRepository) {
            this.admissionMajorMethodRepository = admissionMajorMethodRepository;
        }

        public void delete(Long admissionMajorMethodId) {
            admissionMajorMethodRepository.deleteById(admissionMajorMethodId);
        }

        public boolean admissionMajorMethodDoesNotExist(Long admissionMajorMethodId) {
            return !admissionMajorMethodRepository.existsById(admissionMajorMethodId);
        }
    }
}
