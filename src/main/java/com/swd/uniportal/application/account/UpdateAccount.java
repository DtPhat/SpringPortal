package com.swd.uniportal.application.account;

import com.swd.uniportal.application.account.dto.AccountDto;
import com.swd.uniportal.application.account.dto.UpdateAccountDto;
import com.swd.uniportal.application.account.exception.UserNotAllowedToUpdateRoleException;
import com.swd.uniportal.application.account.exception.UserNotAllowedToUpdateStatusException;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.exception.AccountNotFoundException;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.domain.account.Status;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.config.security.CustomSecurityUtils;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateAccount {

    @RestController
    @Tag(name = "accounts")
    public static final class UpdateAccountController extends BaseController {

        private final UpdateAccountService service;

        @Autowired
        public UpdateAccountController(UpdateAccountService service) {
            this.service = service;
        }

        @PutMapping("accounts/{id}")
        @Operation(summary = "Update an account based on its id.")
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
                responseCode = "403",
                description = "No permission to update account with current role.",
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
        public ResponseEntity<Object> update(
                @PathVariable(name = "id") Long id,
                @RequestBody UpdateAccountDto request) {
            if (Objects.isNull(id) || (id < 1)) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Id is null or not positive.")));
            }
            try {
                return ResponseEntity.ok(service.updateAccountById(id, request));
            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FailedResponse(List.of(e.getMessage())));
            } catch (UserNotAllowedToUpdateRoleException | UserNotAllowedToUpdateStatusException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new FailedResponse(List.of(e.getMessage())));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(List.of("Server error."));
            }
        }
    }

    @Service
    @Transactional(
            rollbackFor = {
                    AccountNotFoundException.class, UserNotAllowedToUpdateRoleException.class,
                    UserNotAllowedToUpdateStatusException.class, RuntimeException.class
    })
    public static class UpdateAccountService {

        private final UpdateAccountDatasource datasource;
        private final CustomSecurityUtils securityUtils;

        @Autowired
        public UpdateAccountService(UpdateAccountDatasource datasource, CustomSecurityUtils securityUtils) {
            this.datasource = datasource;
            this.securityUtils = securityUtils;
        }

        public AccountDto updateAccountById(Long id, UpdateAccountDto request)
                throws AccountNotFoundException, UserNotAllowedToUpdateRoleException,
                UserNotAllowedToUpdateStatusException {
            Account accountToUpdate = datasource.getAccountById(id)
                    .orElseThrow(() -> new AccountNotFoundException(String
                            .format("Account with id '%d' not found", id)));
            validateRequest(request, accountToUpdate.getRole());
            if (Objects.nonNull(request.getRole())) {
                accountToUpdate.setRole(request.getRole());
            }
            if (Objects.nonNull(request.getStatus())) {
                accountToUpdate.setStatus(request.getStatus());
            }
            accountToUpdate = datasource.updateAccount(accountToUpdate);
            return AccountDto.builder()
                    .id(accountToUpdate.getId())
                    .email(accountToUpdate.getEmail())
                    .role(accountToUpdate.getRole().name())
                    .status(accountToUpdate.getStatus().name())
                    .avatarLink(accountToUpdate.getAvatarLink())
                    .build();
        }

        private void validateRequest(UpdateAccountDto request, Role targetRole)
                throws UserNotAllowedToUpdateRoleException, UserNotAllowedToUpdateStatusException {
            if (Objects.nonNull(request.getRole()) && userIsNotAllowedToUpdateRole()) {
                throw new UserNotAllowedToUpdateRoleException(String
                        .format("Current user is not allowed to update role to '%s'", request.getRole().name()));
            }
            if (Objects.nonNull(request.getStatus())
                    && userIsNotAllowedToUpdateStatus(request.getStatus(), targetRole)) {
                throw new UserNotAllowedToUpdateStatusException(String
                        .format("Current user is not allowed to update status to '%s'", request.getRole().name()));
            }
        }

        private boolean userIsNotAllowedToUpdateStatus(Status status, Role targetRole) {
            if (status == Status.UNVALIDATED) {
                return true;
            }
            Set<Role> currentRoles = securityUtils.getCurrentUserRoles();
            if (currentRoles.contains(Role.ADMIN)) {
                return false;
            }
            if (currentRoles.contains(Role.STUDENT)) {
                return true;
            }
            return (currentRoles.contains(Role.STAFF) && (targetRole != Role.STUDENT));
        }

        private boolean userIsNotAllowedToUpdateRole() {
            Set<Role> currentRoles = securityUtils.getCurrentUserRoles();
            return !currentRoles.contains(Role.ADMIN);
        }
    }

    @Datasource
    public static final class UpdateAccountDatasource {

        private final AccountRepository accountRepository;

        @Autowired
        public UpdateAccountDatasource(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public Optional<Account> getAccountById(Long id) {
            return accountRepository.findById(id);
        }

        public Account updateAccount(Account accountToUpdate) {
            return accountRepository.save(accountToUpdate);
        }
    }
}
