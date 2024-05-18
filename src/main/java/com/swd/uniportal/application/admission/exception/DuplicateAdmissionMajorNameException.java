package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class DuplicateAdmissionMajorNameException extends Exception {

    public DuplicateAdmissionMajorNameException() {
    }

    public DuplicateAdmissionMajorNameException(String message) {
        super(message);
    }

    public DuplicateAdmissionMajorNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateAdmissionMajorNameException(Throwable cause) {
        super(cause);
    }
}
