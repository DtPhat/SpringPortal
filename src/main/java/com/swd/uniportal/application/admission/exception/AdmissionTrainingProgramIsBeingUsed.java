package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionTrainingProgramIsBeingUsed extends Exception {

    public AdmissionTrainingProgramIsBeingUsed() {
    }

    public AdmissionTrainingProgramIsBeingUsed(String message) {
        super(message);
    }

    public AdmissionTrainingProgramIsBeingUsed(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionTrainingProgramIsBeingUsed(Throwable cause) {
        super(cause);
    }
}
