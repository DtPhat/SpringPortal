package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionMajorNotInCurrentPlanException extends Exception {

    public AdmissionMajorNotInCurrentPlanException() {
    }

    public AdmissionMajorNotInCurrentPlanException(String message) {
        super(message);
    }

    public AdmissionMajorNotInCurrentPlanException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionMajorNotInCurrentPlanException(Throwable cause) {
        super(cause);
    }
}
