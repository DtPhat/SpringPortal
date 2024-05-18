package com.swd.uniportal.application.login;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.CustomValidation;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.authentication.JwtService;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DoLogin {

    @Data
    public static class DoLoginRequest {

        @NotBlank(message = "Email must not be blank or null.")
        @Email(message = "Email must have valid structure.")
        @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters.")
        String email;

        @NotBlank(message = "Password must not be blank or null.")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
        String password;
    }

    @RestController
    @Tag(name = "login")
    public static class DoLoginController extends BaseController {

        private final DoLoginService doLoginService;

        @Autowired
        public DoLoginController(DoLoginService doLoginService) {
            this.doLoginService = doLoginService;
        }

        @PostMapping("/auth/login")
        @Operation(summary = "Login an account")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Login credential",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DoLoginRequest.class)
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
        public ResponseEntity<Object> login(@RequestBody DoLoginRequest doLoginRequest) {
            List<String> violations = CustomValidation.validate(doLoginRequest);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest().body(new FailedResponse(violations));
            }
            try {
                JwtTokenResponse response = doLoginService.login(doLoginRequest);
                return ResponseEntity.ok(response);
            } catch (AccountNotFoundException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of(e.getMessage())));
            } catch (BadCredentialsException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Invalid email or password.")));
            } catch (DisabledException e) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Account is inactive.")));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Unknown error (server error).")));
            }
        }
    }

    @Service
    public static class DoLoginService {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final DoLoginDatasource datasource;

        @Autowired
        public DoLoginService(AuthenticationManager authenticationManager, JwtService jwtService,
                              DoLoginDatasource datasource) {
            this.authenticationManager = authenticationManager;
            this.jwtService = jwtService;
            this.datasource = datasource;
        }

        public JwtTokenResponse login(DoLoginRequest loginDto) throws AccountNotFoundException {
            UserDetails userDetails = datasource.getUserDetailsByEmail(loginDto.email);
            if (Objects.isNull(userDetails)) {
                throw new AccountNotFoundException(String.format("Account with email \"%s\" not found.",
                        loginDto.email));
            }
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDto.email, loginDto.password));
            String token = jwtService.generateToken(userDetails);
            return new JwtTokenResponse(token, JwtTokenType.BEARER.capitalize());
        }
    }

    @Datasource
    public static final class DoLoginDatasource {

        private final AccountRepository accountRepository;

        @Autowired
        public DoLoginDatasource(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public UserDetails getUserDetailsByEmail(String email) {
            return accountRepository.findAccountByEmail(email).orElse(null);
        }
    }
}
