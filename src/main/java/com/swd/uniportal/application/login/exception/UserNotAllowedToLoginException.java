package com.swd.uniportal.application.login.exception;

@SuppressWarnings("unused")
public class UserNotAllowedToLoginException extends Exception {

    public UserNotAllowedToLoginException() {
    }

    public UserNotAllowedToLoginException(String message) {
        super(message);
    }

    public UserNotAllowedToLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotAllowedToLoginException(Throwable cause) {
        super(cause);
    }
}
