package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.account.Login;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, Long> {

    Optional<Login> findLoginByAccount(Account account);
}