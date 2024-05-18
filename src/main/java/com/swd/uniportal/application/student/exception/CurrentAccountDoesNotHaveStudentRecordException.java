package com.swd.uniportal.application.student.exception;

@SuppressWarnings("unused")
public class CurrentAccountDoesNotHaveStudentRecordException extends Exception {

    public CurrentAccountDoesNotHaveStudentRecordException() {
    }

    public CurrentAccountDoesNotHaveStudentRecordException(String message) {
        super(message);
    }

    public CurrentAccountDoesNotHaveStudentRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrentAccountDoesNotHaveStudentRecordException(Throwable cause) {
        super(cause);
    }
}
