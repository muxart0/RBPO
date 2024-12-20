package ru.mtuci.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//TODO: 1. Интересное решение

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LicenseException.class)
    public ResponseEntity<?> handleLicenseException(LicenseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<?> handleRProductException(ProductException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ActivationException.class)
    public ResponseEntity<?> handleActivationException(ActivationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyCreate.class)
    public ResponseEntity<?> handleUserAlreadyCreateException(UserAlreadyCreate ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(LicenseNotFoundException.class)
    public ResponseEntity<?> handleLicenseNotFoundException(LicenseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<?> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> handleUserException(UserException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DeviceException.class)
    public ResponseEntity<?> handleDeviceException(DeviceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка: " + ex.getMessage());
    }


}