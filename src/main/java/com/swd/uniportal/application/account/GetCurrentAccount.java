package com.swd.uniportal.application.account;

import com.swd.uniportal.application.account.dto.AccountDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetCurrentAccount {

    @RestController
    @Tag(name = "accounts")
    public static final class GetCurrentAccountController extends BaseController {

        private final GetCurrentAccountService service;
        private final CustomSecurityUtils securityUtils;

        @Autowired
        public GetCurrentAccountController(GetCurrentAccountService service, CustomSecurityUtils securityUtils) {
            this.service = service;
            this.securityUtils = securityUtils;
        }

        @GetMapping("/accounts/current")
        @Operation(summary = "Get current user's account.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AccountDto.class)
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
                responseCode = "401",
                description = "Account unauthorized.",
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
                String currentEmail = securityUtils.getCurrentUserEmail();
                if (StringUtils.isBlank(currentEmail)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new FailedResponse(List.of("Not authenticated yet to get account details.")));
                }
                return ResponseEntity.ok(service.get(currentEmail));
            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    public static final class GetCurrentAccountService {

        private final GetCurrentAccountDatasource datasource;

        @Autowired
        public GetCurrentAccountService(GetCurrentAccountDatasource datasource) {
            this.datasource = datasource;
        }

        public AccountDto get(String currentEmail) throws AccountNotFoundException {
            Account account = datasource.getAccountByEmail(currentEmail)
                    .orElseThrow(() -> new AccountNotFoundException("Cannot find account with current user."));
            return AccountDto.builder()
                    .id(account.getId())
                    .email(account.getEmail())
                    .firstName(account.getFirstName())
                    .lastName(account.getLastName())
                    .role(account.getRole().name())
                    .status(account.getStatus().name())
                    .avatarLink(account.getAvatarLink())
                    .build();
        }
    }

    @Datasource
    public static final class GetCurrentAccountDatasource {

        private final AccountRepository accountRepository;

        @Autowired
        public GetCurrentAccountDatasource(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public Optional<Account> getAccountByEmail(String email) {
            return accountRepository.findAccountByEmail(email);
        }
    }
}
