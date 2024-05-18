package com.swd.uniportal.infrastructure.converter;

import com.swd.uniportal.domain.account.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements Converter<String, Role> {

    @Override
    public Role convert(@NonNull String source) {
        return Role.valueOf(source);
    }
}
