package com.swd.uniportal.infrastructure.config.exception_handling;

import com.swd.uniportal.application.common.FailedResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FailedResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        Class<?> requiredType = e.getRequiredType();
        String message = String.format("Invalid param '%s': expect '%s' but is '%s'.", e.getName(),
                Objects.isNull(requiredType) ? null : requiredType.getSimpleName(), e.getValue());
        return ResponseEntity.badRequest().body(new FailedResponse(List.of(message)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FailedResponse> handleTypeMismatch() {
        return ResponseEntity.badRequest().body(new FailedResponse(List
                .of("Invalid type conversion. Check input again for correct type.")));
    }
}
