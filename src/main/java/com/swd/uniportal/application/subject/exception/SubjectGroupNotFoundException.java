package com.swd.uniportal.application.subject.exception;

@SuppressWarnings("unused")
public class SubjectGroupNotFoundException extends Exception {

    public SubjectGroupNotFoundException() {
    }

    public SubjectGroupNotFoundException(String message) {
        super(message);
    }

    public SubjectGroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubjectGroupNotFoundException(Throwable cause) {
        super(cause);
    }
}
