package com.swd.uniportal.application.student.exception;

public class StudentRecordNotFoundException extends Exception{
    public StudentRecordNotFoundException() {
    }

    public StudentRecordNotFoundException(String message) {
        super(message);
    }

    public StudentRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentRecordNotFoundException(Throwable cause) {
        super(cause);
    }
}
