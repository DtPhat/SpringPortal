package com.swd.uniportal.application.student;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.high_school.exception.HighSchoolNotFoundException;
import com.swd.uniportal.application.student.dto.StudentDto;
import com.swd.uniportal.application.student.dto.UpdatedStudentDto;
import com.swd.uniportal.application.student.exception.StudentNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.institution.HighSchool;
import com.swd.uniportal.domain.student.Student;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
import com.swd.uniportal.infrastructure.repository.HighSchoolRepository;
import com.swd.uniportal.infrastructure.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateCurrentStudent {

    @RestController
    @Tag(name = "students")
    public static final class UpdateCurrentStudentController extends BaseController {

        private final UpdateCurrentStudentService service;

        @Autowired
        public UpdateCurrentStudentController(UpdateCurrentStudentService service) {
            this.service = service;
        }

        @PutMapping("/students/current")
        @Operation(summary = "Update current student.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Student to update.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdatedStudentDto.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = StudentDto.class)
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
        public ResponseEntity<Object> updateStudentRecord(
                @RequestBody UpdatedStudentDto request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                StudentDto response =  service.update(request);
                return ResponseEntity.ok(response);
            } catch (AccountNotFoundException
                     | StudentNotFoundException
                     | HighSchoolNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class UpdateCurrentStudentService {

        private final UpdateCurrentStudentDatasource datasource;
        private final CustomSecurityUtils customSecurityUtils;
        private final Mapper<Student, StudentDto> mapper;

        @Autowired
        public UpdateCurrentStudentService(
                UpdateCurrentStudentDatasource datasource,
                CustomSecurityUtils customSecurityUtils,
                Mapper<Student, StudentDto> mapper) {
            this.datasource = datasource;
            this.customSecurityUtils = customSecurityUtils;
            this.mapper = mapper;
        }

        public StudentDto update(UpdatedStudentDto request)
                throws AccountNotFoundException,
                StudentNotFoundException,
                HighSchoolNotFoundException {
            Account currentAccount = customSecurityUtils.getCurrentAccount();
            if (Objects.isNull(currentAccount)) {
                throw new AccountNotFoundException("Current student account not found.");
            }
            HighSchool highSchool = datasource.getHighSchool(request.getHighSchoolId())
                    .orElseThrow(() -> new HighSchoolNotFoundException(String
                            .format("High school with id '%d' not found.", request.getHighSchoolId())));
            Student student = datasource.getStudentByAccount(currentAccount.getId())
                    .orElseThrow(() -> new StudentNotFoundException("Cannot get current student account."));
            student.setBirthDate(request.getBirthDate());
            student.setPhone(StringUtils.trim(request.getPhone()));
            student.setHighSchool(highSchool);
            student = datasource.save(student);
            return mapper.toDto(student);
        }
    }

    @Datasource
    public static final class UpdateCurrentStudentDatasource {

        private final StudentRepository studentRepository;
        private final HighSchoolRepository highSchoolRepository;

        @Autowired
        public UpdateCurrentStudentDatasource(
                StudentRepository studentRepository,
                HighSchoolRepository highSchoolRepository) {
            this.studentRepository = studentRepository;
            this.highSchoolRepository = highSchoolRepository;
        }

        public Optional<Student> getStudentByAccount(Long id) {
            return studentRepository.getByAccountPopulated(id);
        }

        public Optional<HighSchool> getHighSchool(Long highSchoolId) {
            return highSchoolRepository.findById(highSchoolId);
        }

        public Student save(Student student) {
            return studentRepository.save(student);
        }
    }
}
