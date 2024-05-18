package com.swd.uniportal.application.student.record;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.student.dto.StudentRecordDto;
import com.swd.uniportal.application.student.dto.StudentRecordsDto;
import com.swd.uniportal.application.student.dto.UpdatedStudentRecordDto;
import com.swd.uniportal.application.student.exception.StudentNotFoundException;
import com.swd.uniportal.application.student.exception.StudentRecordNotFoundException;
import com.swd.uniportal.application.student.exception.SubjectNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.student.Student;
import com.swd.uniportal.domain.student.StudentRecord;
import com.swd.uniportal.domain.subject.Subject;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
import com.swd.uniportal.infrastructure.repository.StudentRecordRepository;
import com.swd.uniportal.infrastructure.repository.StudentRepository;
import com.swd.uniportal.infrastructure.repository.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCurrentStudentRecord {

    @Builder
    public record StudentRecordResponse(Long id, String name, String description, Float mark) {
    }

    @Builder
    record StudentRecordsResponse(List<StudentRecordResponse> studentRecords) {
    }

    @RestController
    @Tag(name = "students")
    @AllArgsConstructor
    public static class UpdateCurrentStudentRecordController extends BaseController {

        private final UpdateCurrentStudentRecordService service;

        @PutMapping("/students/current/records")
        @Operation(summary = "Update student records of current student.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "student records to update.",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(
                                items = @Schema(implementation = UpdatedStudentRecordDto.class)
                        )
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = StudentRecordsResponse.class)
                )
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
        public ResponseEntity<Object> updateStudentRecord(
                @RequestBody List<UpdatedStudentRecordDto> request) {
            List<String> violations = new ArrayList<>();
            List<String> finalViolations = violations;
            request.forEach(sr -> finalViolations.addAll(CustomValidation.validate(sr)));
            violations = violations.stream().distinct().toList();
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                StudentRecordsDto response =  service.update(request);
                return ResponseEntity.ok(response);
            } catch (StudentNotFoundException | SubjectNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional(rollbackFor = Exception.class)
    public static class UpdateCurrentStudentRecordService {

        private final UpdateCurrentStudentRecordDatasource datasource;
        private final EntityManager em;
        private final CustomSecurityUtils customSecurityUtils;
        private final Mapper<StudentRecord, StudentRecordDto> mapper;

        public StudentRecordsDto update(List<UpdatedStudentRecordDto> request)
                throws StudentNotFoundException,
                SubjectNotFoundException,
                StudentRecordNotFoundException,
                AccountNotFoundException {
            Account currentAccount = customSecurityUtils.getCurrentAccount();
            if (Objects.isNull(currentAccount)) {
                throw new AccountNotFoundException("Current student account not found.");
            }
            Student student = datasource.getStudentByAccount(currentAccount.getId())
                    .orElseThrow(() -> new StudentNotFoundException("Cannot get current student account."));

            //remove
            Set<Long> updatedRecordIds = new HashSet<>();
            for (UpdatedStudentRecordDto updatedRecord : request) {
                updatedRecordIds.add(updatedRecord.getId());
            }

            List<StudentRecord> recordsToRemove = new ArrayList<>();
            for (StudentRecord studentRecord : student.getStudentRecords()) {
                if (!updatedRecordIds.contains(studentRecord.getId())) {
                    recordsToRemove.add(studentRecord);
                }
            }

            for (StudentRecord studentRecord : recordsToRemove) {
                student.removeStudentRecord(studentRecord);
            }

            em.flush();

            //create
            for (UpdatedStudentRecordDto updatedRecord : request) {
                if (updatedRecord.getId() == null) {
                    Subject subject = datasource.getSubject(updatedRecord.getSubjectId());

                    StudentRecord sr = StudentRecord.builder()
                            .mark(updatedRecord.getMark())
                            .student(student)
                            .subject(subject).build();

                    student.addStudentRecord(sr);
                }
            }

            em.flush();

            //update
            for (UpdatedStudentRecordDto updatedRecord : request) {
                if (updatedRecord.getId() != null) {
                    StudentRecord sr = datasource.getStudentRecord(updatedRecord.getId());
                    Subject subject = datasource.getSubject(updatedRecord.getSubjectId());

                    sr.setMark(updatedRecord.getMark());
                    sr.setSubject(subject);
                }
            }

            return StudentRecordsDto.builder()
                    .size(student.getStudentRecords().size())
                    .studentRecords(student.getStudentRecords().stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class UpdateCurrentStudentRecordDatasource {

        private final StudentRecordRepository studentRecordRepository;
        private final StudentRepository studentRepository;
        private final SubjectRepository subjectRepository;

        public Optional<Student> getStudentByAccount(Long id) {
            return studentRepository.getByAccountPopulated(id);
        }

        public Subject getSubject(Long id) throws SubjectNotFoundException {
            return subjectRepository.findById(id).orElseThrow(
                    () -> new SubjectNotFoundException(String.format("Subject with id %d not found", id))
            );
        }

        public StudentRecord getStudentRecord(Long id) throws StudentRecordNotFoundException {
            return studentRecordRepository.findById(id).orElseThrow(
                    () -> new StudentRecordNotFoundException(String.format("Student record with id %d not found", id))
            );
        }
    }
}
