package com.swd.uniportal.application.account.exception;

@SuppressWarnings("unused")
public class UserNotAllowedToUpdateRoleException extends Exception {

    public UserNotAllowedToUpdateRoleException() {
    }

    public UserNotAllowedToUpdateRoleException(String message) {
        super(message);
    }

    public UserNotAllowedToUpdateRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotAllowedToUpdateRoleException(Throwable cause) {
        super(cause);
    }
}
