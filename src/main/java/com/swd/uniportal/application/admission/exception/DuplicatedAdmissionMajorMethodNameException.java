package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class DuplicatedAdmissionMajorMethodNameException extends Exception {

    public DuplicatedAdmissionMajorMethodNameException() {
    }

    public DuplicatedAdmissionMajorMethodNameException(String message) {
        super(message);
    }

    public DuplicatedAdmissionMajorMethodNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedAdmissionMajorMethodNameException(Throwable cause) {
        super(cause);
    }
}
