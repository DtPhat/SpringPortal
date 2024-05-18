package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionMethodNotFound extends Exception {

    public AdmissionMethodNotFound() {
    }

    public AdmissionMethodNotFound(String message) {
        super(message);
    }

    public AdmissionMethodNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionMethodNotFound(Throwable cause) {
        super(cause);
    }
}
