package com.swd.uniportal.infrastructure.converter;

import com.swd.uniportal.domain.account.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StatusConverter implements Converter<String, Status> {

    @Override
    public Status convert(@NonNull String source) {
        return Status.valueOf(source);
    }
}
