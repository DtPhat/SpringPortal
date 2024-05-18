package com.swd.uniportal.infrastructure.config.security;

import com.swd.uniportal.infrastructure.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public final class CustomUserDetailService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    public CustomUserDetailService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findAccountByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found."));
    }
}
