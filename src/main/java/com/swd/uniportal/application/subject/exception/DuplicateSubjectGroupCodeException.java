package com.swd.uniportal.application.subject.exception;

@SuppressWarnings("unused")
public class DuplicateSubjectGroupCodeException extends Exception {

    public DuplicateSubjectGroupCodeException() {
    }

    public DuplicateSubjectGroupCodeException(String message) {
        super(message);
    }

    public DuplicateSubjectGroupCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateSubjectGroupCodeException(Throwable cause) {
        super(cause);
    }
}
