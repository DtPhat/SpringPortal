package com.swd.uniportal.application.account;

import com.swd.uniportal.application.account.dto.AccountDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetAccount {

    @RestController
    @Tag(name = "accounts")
    public static final class GetAccountController extends BaseController {

        private final GetAccountService service;

        @Autowired
        public GetAccountController(GetAccountService service) {
            this.service = service;
        }

        @GetMapping("/accounts/{id}")
        @Operation(summary = "Get an account based on its id.")
        @ApiResponse(
                responseCode = "200",
                description = "Account found.",
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
                responseCode = "404",
                description = "Account not found.",
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
        public ResponseEntity<Object> get(@PathVariable("id") Long id) {
            try {
                return ResponseEntity.ok(service.get(id));
            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error")));
            }
        }
    }

    @Service
    public static final class GetAccountService {

        private final GetAccountDatasource datasource;

        @Autowired
        public GetAccountService(GetAccountDatasource datasource) {
            this.datasource = datasource;
        }

        public AccountDto get(Long id) throws AccountNotFoundException {
            Account account = datasource.getAccountById(id)
                    .orElseThrow(() -> new AccountNotFoundException(String
                            .format("Account with id '%d' not found.", id)));
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
    public static final class GetAccountDatasource {

        private final AccountRepository accountRepository;

        @Autowired
        public GetAccountDatasource(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }
    }
}
