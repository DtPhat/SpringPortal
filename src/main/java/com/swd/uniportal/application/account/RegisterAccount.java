package com.swd.uniportal.application.account;

import com.swd.uniportal.application.account.dto.RegisterAccountDto;
import com.swd.uniportal.application.account.exception.DuplicatedEmailException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.account.Login;
import com.swd.uniportal.domain.account.LoginMethod;
import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.domain.account.Status;
import com.swd.uniportal.domain.student.Student;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.LoginRepository;
import com.swd.uniportal.infrastructure.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegisterAccount {

    @Builder
    public record AccountCreatedResponse(Long id, String email, String role, String status, String avatarLink) {
    }

    @RestController
    @Tag(name = "accounts")
    public static class RegisterAccountController extends BaseController {

        private final RegisterAccountService service;
        private final CustomSecurityUtils securityUtils;

        @Autowired
        public RegisterAccountController(RegisterAccountService service, CustomSecurityUtils securityUtils) {
            this.service = service;
            this.securityUtils = securityUtils;
        }

        @PostMapping("/accounts")
        @Operation(summary = "Register an account.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Account info to register.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = RegisterAccountDto.class)
                )
        )
        @ApiResponse(
                responseCode = "201",
                description = "Successfully created.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AccountCreatedResponse.class)
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
                responseCode = "403",
                description = "No permission to create with current role.",
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
        public ResponseEntity<Object> register(@RequestBody RegisterAccountDto request) {
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            if (!userIsAllowedToCreateThisAccount(request.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new FailedResponse(List
                        .of("Current role is not allowed to create this account.")));
            }
            try {
                AccountCreatedResponse response = service.register(request);
                return ResponseEntity.created(new URI("")).body(response);
            } catch (DuplicatedEmailException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }

        private boolean userIsAllowedToCreateThisAccount(Role role) {
            try {
                Set<Role> currentRoles = securityUtils.getCurrentUserRoles();
                if (currentRoles.contains(Role.ADMIN)) {
                    return true;
                }
                return (role == Role.STUDENT);
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Service
    @Transactional
    public static class RegisterAccountService {

        private final RegisterAccountDatasource datasource;
        private final PasswordEncoder passwordEncoder;

        @Autowired
        public RegisterAccountService(RegisterAccountDatasource datasource,
                                      PasswordEncoder passwordEncoder) {
            this.datasource = datasource;
            this.passwordEncoder = passwordEncoder;
        }

        public AccountCreatedResponse register(RegisterAccountDto request) throws DuplicatedEmailException {
            Account account = Account.builder()
                    .firstName(request.getFirstName())
                    .email(StringUtils.trim(request.getEmail()))
                    .avatarLink(StringUtils.trimToNull(request.getAvatarLink()))
                    .role(request.getRole())
                    .status(Status.ACTIVE)
                    .build();
            if (datasource.emailAlreadyExists(account.getEmail())) {
                throw new DuplicatedEmailException(String.format("Email %s already exists.", account.getEmail()));
            }
            Login login = Login.builder()
                    .method(LoginMethod.DEFAULT)
                    .password(passwordEncoder.encode(StringUtils.trim(request.getPassword())))
                    .build();
            if (request.getRole() == Role.STUDENT) {
                Student student = new Student();
                account = datasource.persist(account, login, student);
            } else {
                account = datasource.persist(account, login);
            }
            return AccountCreatedResponse.builder()
                    .id(account.getId())
                    .email(account.getEmail())
                    .role(account.getRole().name())
                    .status(account.getStatus().name())
                    .avatarLink(account.getAvatarLink())
                    .build();
        }
    }

    @Datasource
    public static class RegisterAccountDatasource {

        private final AccountRepository accountRepository;
        private final LoginRepository loginRepository;
        private final StudentRepository studentRepository;

        @Autowired
        public RegisterAccountDatasource(
                AccountRepository accountRepository,
                LoginRepository loginRepository,
                StudentRepository studentRepository) {
            this.accountRepository = accountRepository;
            this.loginRepository = loginRepository;
            this.studentRepository = studentRepository;
        }

        public boolean emailAlreadyExists(String email) {
            return accountRepository.existsAccountByEmail(email);
        }

        public Account persist(Account account, Login login) {
            Account savedAccount = accountRepository.save(account);
            login.setAccount(savedAccount);
            loginRepository.save(login);
            return savedAccount;
        }

        public Account persist(Account account, Login login, Student student) {
            Account savedAccount = accountRepository.save(account);
            login.setAccount(savedAccount);
            loginRepository.save(login);
            student.setAccount(savedAccount);
            studentRepository.save(student);
            return savedAccount;
        }
    }
}
