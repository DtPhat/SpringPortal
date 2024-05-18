package com.swd.uniportal.application.subject.exception;

@SuppressWarnings("unused")
public class SubjectNotFoundException extends Exception {

    public SubjectNotFoundException() {
    }

    public SubjectNotFoundException(String message) {
        super(message);
    }

    public SubjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubjectNotFoundException(Throwable cause) {
        super(cause);
    }
}
