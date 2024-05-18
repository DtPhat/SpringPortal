package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.account.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByEmail(String email);

    Boolean existsAccountByEmail(String email);
}