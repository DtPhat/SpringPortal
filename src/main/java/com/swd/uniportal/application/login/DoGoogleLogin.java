package com.swd.uniportal.application.login;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.gson.GsonFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.application.login.exception.InvalidGoogleIdTokenException;
import com.swd.uniportal.application.login.exception.UserNotAllowedToLoginException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.account.Login;
import com.swd.uniportal.domain.account.LoginMethod;
import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.domain.account.Status;
import com.swd.uniportal.domain.student.Student;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.authentication.JwtService;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import com.swd.uniportal.infrastructure.repository.LoginRepository;
import com.swd.uniportal.infrastructure.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DoGoogleLogin {

    @Data
    public static final class DoGoogleLoginRequest {

        @NotBlank(message = "Id token must not be null or blank.")
        private String idToken;
    }

    @RestController
    @Tag(name = "login")
    public static final class DoGoogleLoginController extends BaseController {

        private final DoGoogleLoginService service;

        @Autowired
        public DoGoogleLoginController(DoGoogleLoginService service) {
            this.service = service;
        }

        @PostMapping("/auth/login/google")
        @Operation(summary = "Login account using google id token.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Login credential.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DoGoogleLoginRequest.class)
                )
        )
        @ApiResponse(
                responseCode = "200",
                description = "Logged in successfully and returns a JWT token.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = JwtTokenResponse.class)
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
        public ResponseEntity<Object> login(@RequestBody DoGoogleLoginRequest request) {
            if (Objects.isNull(request)) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Request body is null.")));
            }
            List<String> violations = CustomValidation.validate(request);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                JwtTokenResponse response = service.doGoogleLogin(request);
                return ResponseEntity.ok(response);
            } catch (InvalidGoogleIdTokenException | AccountNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (UserNotAllowedToLoginException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    @Transactional(rollbackFor = Exception.class)
    public static class DoGoogleLoginService {

        @Value("${uniportal.google.client-ids}")
        private String[] clientIds;

        @Value("${jwt.token.lifetime}")
        private Long tokenExp;

        private final DoGoogleLoginDatasource datasource;
        private final JwtService jwtService;

        @Autowired
        public DoGoogleLoginService(DoGoogleLoginDatasource datasource, JwtService jwtService) {
            this.datasource = datasource;
            this.jwtService = jwtService;
        }

        public JwtTokenResponse doGoogleLogin(DoGoogleLoginRequest request)
                throws IOException, InvalidGoogleIdTokenException, AccountNotFoundException,
                UserNotAllowedToLoginException {
            GoogleIdToken idToken = GoogleIdToken.parse(new GsonFactory(), request.getIdToken());
            if (idTokenIsInvalid(idToken)) {
                throw new InvalidGoogleIdTokenException("Id token is invalid.");
            }
            String email = idToken.getPayload().getEmail();
            if (datasource.accountDoesNotExist(email)) {
                String firstName = (String) idToken.getPayload().get("given_name");
                if (StringUtils.isBlank(firstName)) {
                    firstName = "Anonymous";
                }
                Account account = Account.builder()
                        .email(email)
                        .firstName(firstName)
                        .role(Role.STUDENT)
                        .status(Status.ACTIVE)
                        .build();
                Login login = Login.builder()
                        .method(LoginMethod.GOOGLE)
                        .build();
                Student student = new Student();
                datasource.saveAccountAndLogin(account, login, student);
            }
            Account account = datasource.getAccountByEmail(email)
                    .orElseThrow(() -> new AccountNotFoundException("Account with email not found."));
            if (account.getStatus() != Status.ACTIVE || account.getRole() != Role.STUDENT) {
                throw new UserNotAllowedToLoginException("User is not allowed to login with current token id.");
            }
            String jwtToken = jwtService.generateToken(account);
            return new JwtTokenResponse(jwtToken, JwtTokenType.BEARER.capitalize());
        }

        private boolean idTokenIsInvalid(GoogleIdToken idToken) {
            if (!idToken.verifyIssuer("https://accounts.google.com")
                    && !idToken.verifyIssuer("accounts.google.com")) {
                return true;
            }
            if (!idToken.verifyExpirationTime(System.currentTimeMillis(), tokenExp)) {
                return true;
            }
            for (String clientId : clientIds) {
                if (idToken.verifyAudience(Collections.singletonList(clientId))) {
                    return false;
                }
            }
            return true;
        }
    }

    @Datasource
    public static class DoGoogleLoginDatasource {

        private final AccountRepository accountRepository;
        private final LoginRepository loginRepository;
        private final StudentRepository studentRepository;

        @Autowired
        public DoGoogleLoginDatasource(
                AccountRepository accountRepository,
                LoginRepository loginRepository,
                StudentRepository studentRepository) {
            this.accountRepository = accountRepository;
            this.loginRepository = loginRepository;
            this.studentRepository = studentRepository;
        }

        public boolean accountDoesNotExist(String email) {
            return !accountRepository.existsAccountByEmail(email);
        }

        public void saveAccountAndLogin(Account account, Login login, Student student) {
            Account savedAccount = accountRepository.save(account);
            login.setAccount(savedAccount);
            loginRepository.save(login);
            student.setAccount(savedAccount);
            studentRepository.save(student);
        }

        public Optional<Account> getAccountByEmail(String email) {
            return accountRepository.findAccountByEmail(email);
        }
    }
}
