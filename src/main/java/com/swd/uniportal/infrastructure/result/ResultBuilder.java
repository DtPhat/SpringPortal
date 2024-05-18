package com.swd.uniportal.infrastructure.result;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public final class ResultBuilder<T> {

    private T value;
    private String code;
    private String message;
    private final boolean successful;

    ResultBuilder(Boolean successful) {
        this.successful = successful;
    }

    public ResultBuilder<T> withValue(T value) {
        this.value = value;
        return this;
    }

    public ResultBuilder<T> withCodeMessage(String code, String message) {
        this.code = StringUtils.trim(code);
        this.message = StringUtils.trim(message);
        return this;
    }

    public Result<T> build() {
        if (!successful && Objects.nonNull(value)) {
            throw new IllegalStateException("Failed result must not have value.");
        }
        if (!successful) {
            Validate.notBlank(code, "Code is null or blank.");
            Validate.notBlank(message, "Message is null or blank");
        }
        return new Result<>(value, code, message, successful);
    }
}
