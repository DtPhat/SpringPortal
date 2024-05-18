package com.swd.uniportal.application.student;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.student.dto.StudentDto;
import com.swd.uniportal.application.student.exception.StudentNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.student.Student;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetCurrentStudent {

    @RestController
    @Tag(name = "students")
    public static final class GetCurrentStudentController extends BaseController {

        private final GetCurrentStudentService service;

        @Autowired
        public GetCurrentStudentController(GetCurrentStudentService service) {
            this.service = service;
        }

        @GetMapping("/students/current")
        @Operation(summary = "Get current student.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = StudentDto.class)
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
                StudentDto student = service.get();
                return ResponseEntity.ok(student);
            } catch (AccountNotFoundException | StudentNotFoundException e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetCurrentStudentService {

        private final GetCurrentStudentDatasource datasource;
        private final CustomSecurityUtils customSecurityUtils;
        private final Mapper<Student, StudentDto> mapper;

        @Autowired
        public GetCurrentStudentService(
                GetCurrentStudentDatasource datasource,
                CustomSecurityUtils customSecurityUtils,
                Mapper<Student, StudentDto> mapper) {
            this.datasource = datasource;
            this.customSecurityUtils = customSecurityUtils;
            this.mapper = mapper;
        }

        public StudentDto get()
                throws AccountNotFoundException,
                StudentNotFoundException {
            Account currentAccount = customSecurityUtils.getCurrentAccount();
            if (Objects.isNull(currentAccount)) {
                throw new AccountNotFoundException("Current student account not found.");
            }
            Student student = datasource.getStudentByAccount(currentAccount.getId())
                    .orElseThrow(() -> new StudentNotFoundException("Cannot get current student account."));
            return mapper.toDto(student);
        }
    }

    @Datasource
    public static final class GetCurrentStudentDatasource {

        private final StudentRepository studentRepository;

        @Autowired
        public GetCurrentStudentDatasource(
                StudentRepository studentRepository) {
            this.studentRepository = studentRepository;
        }

        public Optional<Student> getStudentByAccount(Long id) {
            return studentRepository.getByAccountPopulated(id);
        }
    }
}
