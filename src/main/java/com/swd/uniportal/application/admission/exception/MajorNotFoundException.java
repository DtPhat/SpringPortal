package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class MajorNotFoundException extends Exception {

    public MajorNotFoundException() {
    }

    public MajorNotFoundException(String message) {
        super(message);
    }

    public MajorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MajorNotFoundException(Throwable cause) {
        super(cause);
    }
}
