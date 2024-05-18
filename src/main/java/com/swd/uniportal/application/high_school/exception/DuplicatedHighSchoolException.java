package com.swd.uniportal.application.high_school.exception;

@SuppressWarnings("unused")
public class DuplicatedHighSchoolException extends Exception {

    public DuplicatedHighSchoolException() {
    }

    public DuplicatedHighSchoolException(String message) {
        super(message);
    }

    public DuplicatedHighSchoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedHighSchoolException(Throwable cause) {
        super(cause);
    }
}
