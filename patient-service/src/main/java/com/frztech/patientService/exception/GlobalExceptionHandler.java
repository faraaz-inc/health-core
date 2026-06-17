package com.frztech.patientService.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
                                                            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        //log the error in console
        log.warn("Error Message: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("email", e.getMessage());

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(PatientNotFoundException e) {
        //log the error in console
        log.warn("Error message: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("error message", e.getMessage());

        return ResponseEntity.badRequest().body(errors);

    }
}
