package com.swd.uniportal.application.major.exception;

public class DuplicatedCodeException extends Exception{
    public DuplicatedCodeException() {
    }

    public DuplicatedCodeException(String message) {
        super(message);
    }

    public DuplicatedCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedCodeException(Throwable cause) {
        super(cause);
    }
}
