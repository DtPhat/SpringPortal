package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class AdmissionTrainingProgramNotInAdmissionPlanException extends Exception {

    public AdmissionTrainingProgramNotInAdmissionPlanException() {
    }

    public AdmissionTrainingProgramNotInAdmissionPlanException(String message) {
        super(message);
    }

    public AdmissionTrainingProgramNotInAdmissionPlanException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdmissionTrainingProgramNotInAdmissionPlanException(Throwable cause) {
        super(cause);
    }
}
