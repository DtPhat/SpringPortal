package com.swd.uniportal.application.account;

import com.swd.uniportal.application.account.exception.ToggleUnvalidatedAccountException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.account.Status;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToggleAccountStatus {

    @RestController
    @Tag(name = "accounts")
    public static final class ToggleAccountStatusController extends BaseController {

        private final ToggleAccountStatusService service;

        @Autowired
        public ToggleAccountStatusController(ToggleAccountStatusService service) {
            this.service = service;
        }

        @DeleteMapping("/accounts/{id}")
        @Operation(summary = "Toggle account's active status.")
        @ApiResponse(
                responseCode = "204",
                description = "Successful."
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
        @SuppressWarnings("Duplicates")
        public ResponseEntity<Object> toggle(@PathVariable("id") Long id) {
            if (id < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List
                        .of("Account must be positive.")));
            }
            try {
                service.toggle(id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } catch (AccountNotFoundException | ToggleUnvalidatedAccountException e) {
                return ResponseEntity.badRequest()
                        .body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class ToggleAccountStatusService {

        private final ToggleAccountStatusDatasource datasource;

        @Autowired
        public ToggleAccountStatusService(ToggleAccountStatusDatasource datasource) {
            this.datasource = datasource;
        }

        public void toggle(Long id)
                throws ToggleUnvalidatedAccountException,
                AccountNotFoundException {
            Account account = datasource.getAccount(id)
                    .orElseThrow(() -> new AccountNotFoundException(String
                            .format("Account with id '%d' not found.", id)));
            if (account.getStatus() == Status.ACTIVE) {
                account.setStatus(Status.INACTIVE);
            } else if (account.getStatus() == Status.INACTIVE) {
                account.setStatus(Status.ACTIVE);
            } else {
                throw new ToggleUnvalidatedAccountException("Cannot toggle account's status for " + Status.UNVALIDATED);
            }
            datasource.save(account);
        }
    }

    @Datasource
    public static final class ToggleAccountStatusDatasource {

        private final AccountRepository accountRepository;

        @Autowired
        public ToggleAccountStatusDatasource(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public Optional<Account> getAccount(Long id) {
            return accountRepository.findById(id);
        }

        public void save(Account account) {
            accountRepository.save(account);
        }
    }
}
