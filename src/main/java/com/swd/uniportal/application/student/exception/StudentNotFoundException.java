package com.swd.uniportal.application.student.exception;

public class StudentNotFoundException extends Exception{

    public StudentNotFoundException() {
    }

    public StudentNotFoundException(String message) {
        super(message);
    }

    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentNotFoundException(Throwable cause) {
        super(cause);
    }
}
