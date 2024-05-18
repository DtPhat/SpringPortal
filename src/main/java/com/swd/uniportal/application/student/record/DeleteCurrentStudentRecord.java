package com.swd.uniportal.application.student.record;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.student.exception.CurrentAccountDoesNotHaveStudentRecordException;
import com.swd.uniportal.application.student.exception.StudentRecordNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
import com.swd.uniportal.infrastructure.repository.StudentRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteCurrentStudentRecord {

    @RestController
    @Tag(name = "students")
    @AllArgsConstructor
    public static class DeleteCurrentStudentRecordController extends BaseController {

        private final DeleteCurrentStudentRecordService service;

        @DeleteMapping("students/current/records/{id}")
        @Operation(summary = "Delete a student record by ID.")
        @ApiResponse(
                responseCode = "204",
                description = "Successful."
        )
        @ApiResponse(
                responseCode = "404",
                description = "Student record not found.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = FailedResponse.class
                        )
                )
        )
        @ApiResponse(
                responseCode = "500",
                description = "Server error.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = FailedResponse.class
                        )
                )
        )
        public ResponseEntity<Object> deleteStudentRecordById(@PathVariable("id") Long id) {
            try {
                service.deleteById(id);
                return ResponseEntity.noContent().build();
            } catch (StudentRecordNotFoundException
                    | AccountNotFoundException
                    | CurrentAccountDoesNotHaveStudentRecordException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class DeleteCurrentStudentRecordService {

        private final DeleteStudentRecordDatasource datasource;
        private final CustomSecurityUtils customSecurityUtils;

        public void deleteById(Long id)
                throws StudentRecordNotFoundException,
                AccountNotFoundException,
                CurrentAccountDoesNotHaveStudentRecordException {
            if (datasource.studentRecordDoesNotExist(id)) {
                throw new StudentRecordNotFoundException("Student record not found.");
            }
            Account currentAccount = customSecurityUtils.getCurrentAccount();
            if (Objects.isNull(currentAccount)) {
                throw new AccountNotFoundException("Current student account not found.");
            }
            if (datasource.currentAccountDoesNotOwnStudentRecord(currentAccount.getId())) {
                throw new CurrentAccountDoesNotHaveStudentRecordException(
                        "Current student cannot delete other's id.");
            }
            datasource.delete(id);
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class DeleteStudentRecordDatasource {

        private final StudentRecordRepository studentRecordRepository;

        public void delete(Long id) {
            studentRecordRepository.deleteById(id);
        }

        public boolean studentRecordDoesNotExist(Long id) {
            return studentRecordRepository.existsById(id);
        }

        public boolean currentAccountDoesNotOwnStudentRecord(Long id) {
            return studentRecordRepository.existsByIdAndAccount(id);
        }
    }
}
