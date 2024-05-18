package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionTrainingProgramNotFoundException extends Exception {

    public AdmissionTrainingProgramNotFoundException() {
    }

    public AdmissionTrainingProgramNotFoundException(String message) {
        super(message);
    }

    public AdmissionTrainingProgramNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionTrainingProgramNotFoundException(Throwable cause) {
        super(cause);
    }
}
