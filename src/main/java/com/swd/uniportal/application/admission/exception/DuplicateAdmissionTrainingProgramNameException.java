package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class DuplicateAdmissionTrainingProgramNameException extends Exception {

    public DuplicateAdmissionTrainingProgramNameException() {
    }

    public DuplicateAdmissionTrainingProgramNameException(String message) {
        super(message);
    }

    public DuplicateAdmissionTrainingProgramNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateAdmissionTrainingProgramNameException(Throwable cause) {
        super(cause);
    }
}
