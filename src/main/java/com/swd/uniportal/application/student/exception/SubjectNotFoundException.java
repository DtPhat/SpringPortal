package com.swd.uniportal.application.student.exception;

public class SubjectNotFoundException extends Exception{
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
