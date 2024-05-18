package com.swd.uniportal.infrastructure.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Result<T> {

    private T value;
    private String code;
    private String message;
    private boolean successful;

    public static <T> ResultBuilder<T> asSuccess() {
        return new ResultBuilder<>(true);
    }

    public static <T> ResultBuilder<T> asFailure() {
        return new ResultBuilder<>(false);
    }
}
