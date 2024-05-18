package com.swd.uniportal.application.common.exception;

@SuppressWarnings("unused")
public class AccountNotFoundException extends Exception {

    public AccountNotFoundException() {
    }

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNotFoundException(Throwable cause) {
        super(cause);
    }
}
