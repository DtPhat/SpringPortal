package com.swd.uniportal.application.account;

import com.swd.uniportal.application.account.dto.AccountDto;
import com.swd.uniportal.application.account.dto.UpdateCurrentAccountDto;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateCurrentAccount {

    @RestController
    @Tag(name = "accounts")
    public static class UpdateCurrentAccountController extends BaseController {

        private final UpdateCurrentAccountService service;
        private final CustomSecurityUtils securityUtils;

        @Autowired
        public UpdateCurrentAccountController(UpdateCurrentAccountService service, CustomSecurityUtils securityUtils) {
            this.service = service;
            this.securityUtils = securityUtils;
        }

        @PutMapping("accounts/current")
        @Operation(summary = "Update current account.")
        @ApiResponse(
                responseCode = "200",
                description = "Account updated.",
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
        public ResponseEntity<Object> update(@RequestBody UpdateCurrentAccountDto request) {
            String currentEmail = securityUtils.getCurrentUserEmail();
            if (StringUtils.isBlank(currentEmail)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new FailedResponse(List.of("Not authenticated.")));
            }
            try {
                return ResponseEntity.ok(service.updateAccount(currentEmail, request));
            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class UpdateCurrentAccountService {

        private final UpdateCurrentAccountDatasource datasource;

        @Autowired
        public UpdateCurrentAccountService(UpdateCurrentAccountDatasource datasource) {
            this.datasource = datasource;
        }

        public AccountDto updateAccount(String currentEmail, UpdateCurrentAccountDto request)
                throws AccountNotFoundException {
            Account account = datasource.getAccountByEmail(currentEmail)
                    .orElseThrow(() -> new AccountNotFoundException(String
                            .format("Account with email '%s' not found.", currentEmail)));
            String firstName = StringUtils.trim(request.getFirstName());
            if (StringUtils.isNotBlank(firstName)) {
                account.setFirstName(firstName);
            }
            String lastName = StringUtils.trim(request.getLastName());
            if (StringUtils.isNotBlank(lastName)) {
                account.setLastName(lastName);
            }
            String avatarLink = StringUtils.trimToNull(request.getAvatarLink());
            if (StringUtils.isNotBlank(avatarLink)) {
                account.setAvatarLink(avatarLink);
            }
            account = datasource.updateAccount(account);
            return AccountDto.builder()
                    .id(account.getId())
                    .email(account.getEmail())
                    .firstName(account.getFirstName())
                    .lastName(account.getLastName())
                    .avatarLink(account.getAvatarLink())
                    .role(account.getRole().name())
                    .status(account.getStatus().name())
                    .build();
        }
    }

    @Datasource
    public static final class UpdateCurrentAccountDatasource {

        private final AccountRepository accountRepository;

        @Autowired
        public UpdateCurrentAccountDatasource(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public Optional<Account> getAccountByEmail(String currentEmail) {
            return accountRepository.findAccountByEmail(currentEmail);
        }

        public Account updateAccount(Account account) {
            return accountRepository.save(account);
        }
    }
}
