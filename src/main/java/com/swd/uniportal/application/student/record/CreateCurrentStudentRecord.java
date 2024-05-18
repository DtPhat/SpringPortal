package com.swd.uniportal.application.student.record;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.student.dto.CreateStudentRecordDto;
import com.swd.uniportal.application.student.dto.StudentRecordDto;
import com.swd.uniportal.application.student.exception.StudentNotFoundException;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCurrentStudentRecord {

    @RestController
    @Tag(name = "students")
    @AllArgsConstructor
    public static class CreateCurrentStudentRecordController extends BaseController {

        private final CreateCurrentStudentRecordService service;

        @PostMapping("/students/current/records")
        @Operation(summary = "Create a student record.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Student record to create.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CreateStudentRecordDto.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = StudentRecordDto.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body.",
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
        public ResponseEntity<Object> createStudentRecord(
                @RequestBody CreateStudentRecordDto request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            if (0.0 < request.getMark() || request.getMark() > 10.0) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of("Mark must be between 0.0 and 10.0.")));
            }
            try {
                StudentRecordDto response = service.create(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (StudentNotFoundException
                     | SubjectNotFoundException
                     | AccountNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    @AllArgsConstructor
    @Transactional
    public static class CreateCurrentStudentRecordService {

        private final CreateCurrentStudentRecordDatasource datasource;
        private final CustomSecurityUtils customSecurityUtils;
        private final Mapper<StudentRecord, StudentRecordDto> mapper;

        public StudentRecordDto create(CreateStudentRecordDto request)
                throws StudentNotFoundException,
                SubjectNotFoundException,
                AccountNotFoundException {
            Account currentAccount = customSecurityUtils.getCurrentAccount();
            if (Objects.isNull(currentAccount)) {
                throw new AccountNotFoundException("Current account not found.");
            }
            Student student = datasource.findStudent(currentAccount.getId());
            Subject subject = datasource.findSubject(request.getSubjectId());

            StudentRecord studentRecord = StudentRecord.builder()
                    .mark(request.getMark())
                    .student(student)
                    .subject(subject)
                    .build();

            studentRecord = datasource.persist(studentRecord);

            return mapper.toDto(studentRecord);
        }
    }

    @Datasource
    @AllArgsConstructor
    public static class CreateCurrentStudentRecordDatasource {

        private final StudentRecordRepository studentRecordRepository;
        private final StudentRepository studentRepository;
        private final SubjectRepository subjectRepository;

        public Student findStudent(Long id) throws StudentNotFoundException {
            return studentRepository.findByAccount(id)
                    .orElseThrow(() -> new StudentNotFoundException("Student not found"));
        }

        public Subject findSubject(Long id) throws SubjectNotFoundException {
            return subjectRepository.findById(id)
                    .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));
        }

        public StudentRecord persist(StudentRecord studentRecord) {
            return studentRecordRepository.save(studentRecord);
        }
    }
}
