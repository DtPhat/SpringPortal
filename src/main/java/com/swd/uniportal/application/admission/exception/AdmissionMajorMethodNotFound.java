package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionMajorMethodNotFound extends Exception {

    public AdmissionMajorMethodNotFound() {
    }

    public AdmissionMajorMethodNotFound(String message) {
        super(message);
    }

    public AdmissionMajorMethodNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionMajorMethodNotFound(Throwable cause) {
        super(cause);
    }
}
