package com.swd.uniportal.application.account.exception;

@SuppressWarnings("unused")
public final class DuplicatedEmailException extends Exception {

    public DuplicatedEmailException() {
    }

    public DuplicatedEmailException(String message) {
        super(message);
    }

    public DuplicatedEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedEmailException(Throwable cause) {
        super(cause);
    }
}
