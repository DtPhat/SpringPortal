package com.swd.uniportal.application.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomValidation {

    private record EmailValidation(@Email String email){
    }

    public static List<String> validate(Object object) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(object).stream().map(ConstraintViolation::getMessage).toList();
        } catch (Exception e) {
            return List.of("Unable to validate.");
        }
    }

    public static boolean emailIsValid(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        EmailValidation emailValidation = new EmailValidation(email);
        return validate(emailValidation).isEmpty();
    }

    public static boolean emailIsNotValid(String email) {
        return !emailIsValid(email);
    }
}
