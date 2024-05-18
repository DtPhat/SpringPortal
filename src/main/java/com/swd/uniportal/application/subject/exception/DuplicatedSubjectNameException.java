package com.swd.uniportal.application.subject.exception;

@SuppressWarnings("unused")
public class DuplicatedSubjectNameException extends Exception {

    public DuplicatedSubjectNameException() {
    }

    public DuplicatedSubjectNameException(String message) {
        super(message);
    }

    public DuplicatedSubjectNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedSubjectNameException(Throwable cause) {
        super(cause);
    }
}
