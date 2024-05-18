package com.swd.uniportal.application.high_school.exception;

public class HighSchoolNotFoundException extends Exception
{
    public HighSchoolNotFoundException() {
    }

    public HighSchoolNotFoundException(String message) {
        super(message);
    }

    public HighSchoolNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public HighSchoolNotFoundException(Throwable cause) {
        super(cause);
    }
}
