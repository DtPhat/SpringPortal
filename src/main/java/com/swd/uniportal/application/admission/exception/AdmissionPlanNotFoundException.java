package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionPlanNotFoundException extends Exception {

    public AdmissionPlanNotFoundException() {
    }

    public AdmissionPlanNotFoundException(String message) {
        super(message);
    }

    public AdmissionPlanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionPlanNotFoundException(Throwable cause) {
        super(cause);
    }
}
