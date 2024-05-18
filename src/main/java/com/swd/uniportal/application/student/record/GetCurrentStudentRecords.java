package com.swd.uniportal.application.student.record;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.student.dto.StudentRecordDto;
import com.swd.uniportal.application.student.dto.StudentRecordsDto;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.student.StudentRecord;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetCurrentStudentRecords {

    @RestController
    @Tag(name = "students")
    @AllArgsConstructor
    public static class GetCurrentStudentRecordsController extends BaseController {

        private final GetCurrentStudentRecordsService service;

        @GetMapping("/students/current/records")
        @Operation(summary = "Get list of student records by student ID.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = StudentRecordsDto.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid parameters.",
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
        public ResponseEntity<Object> get() {
            try {
                StudentRecordsDto response = service.get();
                return ResponseEntity.ok(response);
            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of("Student not found")));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.", e.getMessage())));
            }
        }
    }


    @Service
    @AllArgsConstructor
    public static class GetCurrentStudentRecordsService {

        private final GetCurrentStudentRecordsDatasource datasource;
        private final CustomSecurityUtils customSecurityUtils;
        private final Mapper<StudentRecord, StudentRecordDto> mapper;

        public StudentRecordsDto get()
                throws AccountNotFoundException {
            Account currentAccount = customSecurityUtils.getCurrentAccount();
            if (Objects.isNull(currentAccount)) {
                throw new AccountNotFoundException("Current student account not found.");
            }
            List<StudentRecord> studentRecords = datasource.getRecords(currentAccount.getId());
            return StudentRecordsDto.builder()
                    .size(studentRecords.size())
                    .studentRecords(studentRecords.stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class GetCurrentStudentRecordsDatasource {

        private final StudentRecordRepository studentRecordRepository;

        public List<StudentRecord> getRecords(Long id) {
            return studentRecordRepository.getByAccount(id);
        }
    }
}
