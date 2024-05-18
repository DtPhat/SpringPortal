package com.swd.uniportal.application.login.exception;

@SuppressWarnings("unused")
public final class InvalidGoogleIdTokenException extends Exception {

    public InvalidGoogleIdTokenException() {
    }

    public InvalidGoogleIdTokenException(String message) {
        super(message);
    }

    public InvalidGoogleIdTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGoogleIdTokenException(Throwable cause) {
        super(cause);
    }
}
