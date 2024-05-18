package com.swd.uniportal.application.admission.exception;

@SuppressWarnings("unused")
public class TrainingProgramNotFoundException extends Exception {

    public TrainingProgramNotFoundException() {
    }

    public TrainingProgramNotFoundException(String message) {
        super(message);
    }

    public TrainingProgramNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrainingProgramNotFoundException(Throwable cause) {
        super(cause);
    }
}
