package com.swd.uniportal.application.account;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.account.dto.AccountDto;
import com.swd.uniportal.application.account.dto.AccountsDto;
import com.swd.uniportal.application.account.dto.GetAccountsDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.account.QAccount;
import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.domain.account.Status;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetAccounts {

    @RestController
    @Tag(name = "accounts")
    public static final class GetAccountsController extends BaseController {

        private final GetAccountsService service;

        @Autowired
        public GetAccountsController(GetAccountsService service) {
            this.service = service;
        }

        @GetMapping("/accounts")
        @Operation(summary = "Get list of accounts.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AccountsDto.class)
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
                description = "No permission to get account list with current role.",
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
        public ResponseEntity<Object> get(
                @RequestParam(name = "search", required = false) String search,
                @RequestParam(name = "role", required = false) Role role,
                @RequestParam(name = "status", required = false) Status status,
                @RequestParam(name = "sort", defaultValue = "ASC") SortOrder sortOrder,
                @RequestParam(name = "page", defaultValue = "1") Long page) {
            try {
                if (page < 1) {
                    return ResponseEntity.badRequest().body(new FailedResponse(List
                            .of("Page must be positive (page > 0)")));
                }
                AccountsDto response =  service.get(GetAccountsDto.builder()
                                .search(StringUtils.trimToNull(search))
                                .role(role)
                                .status(status)
                                .sortOrder(sortOrder)
                                .page(page)
                        .build());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.", e.getMessage())));
            }
        }
    }

    @Service
    public static final class GetAccountsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetAccountsDatasource datasource;

        @Autowired
        public GetAccountsService(GetAccountsDatasource datasource) {
            this.datasource = datasource;
        }

        public AccountsDto get(GetAccountsDto request) {
            List<Account> accounts = datasource.getAccountsFilteredPaginated(request);
            Long totalPage = datasource.getCountOfAccounts() / pageSize;
            return AccountsDto.builder()
                    .page(request.page())
                    .pageSize((long) accounts.size())
                    .totalPages(totalPage)
                    .size((long) accounts.size())
                    .accounts(accounts.stream().map(acc -> AccountDto.builder()
                            .id(acc.getId())
                            .email(acc.getEmail())
                            .role(acc.getRole().name())
                            .status(acc.getStatus().name())
                            .build()).toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetAccountsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final AccountRepository accountRepository;
        private final EntityManager entityManager;

        @Autowired
        public GetAccountsDatasource(AccountRepository accountRepository,
                                     EntityManager entityManager) {
            this.accountRepository = accountRepository;
            this.entityManager = entityManager;
        }

        public List<Account> getAccountsFilteredPaginated(GetAccountsDto request) {
            QAccount account = QAccount.account;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search())) {
                filters.and(account.email.containsIgnoreCase(request.search()));
            }
            if (Objects.nonNull(request.role())) {
                filters.and(account.role.eq(request.role()));
            }
            if (Objects.nonNull(request.status())) {
                filters.and(account.status.eq(request.status()));
            }
            filters.and(account.role.ne(Role.ADMIN));
            return factory.selectFrom(account)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? account.email.desc() : account.email.asc())
                    .limit(pageSize)
                    .fetch();
        }

        public Long getCountOfAccounts() {
            return accountRepository.count();
        }
    }
}
