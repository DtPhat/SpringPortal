package com.swd.uniportal.application.subject.exception;

@SuppressWarnings("unused")
public class InvalidSubjectIdException extends Exception {

    public InvalidSubjectIdException() {
    }

    public InvalidSubjectIdException(String message) {
        super(message);
    }

    public InvalidSubjectIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSubjectIdException(Throwable cause) {
        super(cause);
    }
}
