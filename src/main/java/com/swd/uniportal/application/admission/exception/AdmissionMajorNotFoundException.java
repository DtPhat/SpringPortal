package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionMajorNotFoundException extends Exception {

    public AdmissionMajorNotFoundException() {
    }

    public AdmissionMajorNotFoundException(String message) {
        super(message);
    }

    public AdmissionMajorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionMajorNotFoundException(Throwable cause) {
        super(cause);
    }
}
