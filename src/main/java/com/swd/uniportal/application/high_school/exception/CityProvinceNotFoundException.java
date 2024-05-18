package com.swd.uniportal.application.high_school.exception;

@SuppressWarnings("unused")
public class CityProvinceNotFoundException extends Exception {

    public CityProvinceNotFoundException() {
    }

    public CityProvinceNotFoundException(String message) {
        super(message);
    }

    public CityProvinceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CityProvinceNotFoundException(Throwable cause) {
        super(cause);
    }
}
