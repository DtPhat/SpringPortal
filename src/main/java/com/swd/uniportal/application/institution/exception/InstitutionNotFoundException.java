package com.swd.uniportal.application.institution.exception;

public class InstitutionNotFoundException extends Exception{

    public InstitutionNotFoundException() {
    }

    public InstitutionNotFoundException(String message) {
        super(message);
    }

    public InstitutionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstitutionNotFoundException(Throwable cause) {
        super(cause);
    }

}
