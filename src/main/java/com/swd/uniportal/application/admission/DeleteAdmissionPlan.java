package com.swd.uniportal.application.admission;

import com.swd.uniportal.application.admission.exception.AdmissionPlanNotFoundException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AdmissionPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteAdmissionPlan {

    @RestController
    @Tag(name = "admission-plans")
    public static class DeleteAdmissionPlanController extends BaseController {

        private final DeleteAdmissionPlanService service;

        @Autowired
        public DeleteAdmissionPlanController(DeleteAdmissionPlanService service) {
            this.service = service;
        }

        @DeleteMapping("/admission-plans/{id}")
        @Operation(summary = "Delete admission plan.")
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
        public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
            if (Objects.isNull(id) || id < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Id is null or  not positive.")));
            }
            try {
                service.delete(id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } catch (AdmissionPlanNotFoundException e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    @Transactional(rollbackFor = Exception.class)
    public static class DeleteAdmissionPlanService {

        private final DeleteAdmissionPlanDatasource datasource;

        @Autowired
        public DeleteAdmissionPlanService(DeleteAdmissionPlanDatasource datasource) {
            this.datasource = datasource;
        }

        public void delete(Long id) throws AdmissionPlanNotFoundException {
            if (datasource.admissionPlanDoesNotExist(id)) {
                throw new AdmissionPlanNotFoundException(String.format("Admission plan with id '%d' not found.", id));
            }
            datasource.deleteAdmissionPlanById(id);
        }
    }

    @Datasource
    public static class DeleteAdmissionPlanDatasource {

        private final AdmissionPlanRepository admissionPlanRepository;

        @Autowired
        public DeleteAdmissionPlanDatasource(AdmissionPlanRepository admissionPlanRepository) {
            this.admissionPlanRepository = admissionPlanRepository;
        }

        public boolean admissionPlanDoesNotExist(Long id) {
            return !admissionPlanRepository.existsById(id);
        }

        public void deleteAdmissionPlanById(Long id) {
            admissionPlanRepository.deleteById(id);
        }
    }
}
