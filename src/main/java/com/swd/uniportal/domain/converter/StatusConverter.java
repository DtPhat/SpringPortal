package com.swd.uniportal.domain.converter;

import com.swd.uniportal.domain.account.Status;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status attribute) {
        return attribute.name();
    }

    @Override
    public Status convertToEntityAttribute(String dbData) {
        return Status.valueOf(dbData);
    }
}
