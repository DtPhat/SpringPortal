package com.swd.uniportal.application.account.exception;

@SuppressWarnings("unused")
public class UserNotAllowedToUpdateStatusException extends Exception {

    public UserNotAllowedToUpdateStatusException() {
    }

    public UserNotAllowedToUpdateStatusException(String message) {
        super(message);
    }

    public UserNotAllowedToUpdateStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotAllowedToUpdateStatusException(Throwable cause) {
        super(cause);
    }
}
