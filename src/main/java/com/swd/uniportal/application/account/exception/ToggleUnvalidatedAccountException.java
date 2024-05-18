package com.swd.uniportal.application.account.exception;

@SuppressWarnings("unused")
public class ToggleUnvalidatedAccountException extends Exception {

    public ToggleUnvalidatedAccountException() {
    }

    public ToggleUnvalidatedAccountException(String message) {
        super(message);
    }

    public ToggleUnvalidatedAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public ToggleUnvalidatedAccountException(Throwable cause) {
        super(cause);
    }
}
