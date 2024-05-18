package com.swd.uniportal.infrastructure.converter;

import com.swd.uniportal.infrastructure.common.SortOrder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class SortOrderConverter implements Converter<String, SortOrder> {

    @Override
    public SortOrder convert(String source) {
        return SortOrder.valueOf(source);
    }
}
