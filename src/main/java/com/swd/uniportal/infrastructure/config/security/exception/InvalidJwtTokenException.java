package com.swd.uniportal.infrastructure.config.security.exception;

@SuppressWarnings("unused")
public final class InvalidJwtTokenException extends Exception {

    public InvalidJwtTokenException() {
    }

    public InvalidJwtTokenException(String message) {
        super(message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJwtTokenException(Throwable cause) {
        super(cause);
    }
}
