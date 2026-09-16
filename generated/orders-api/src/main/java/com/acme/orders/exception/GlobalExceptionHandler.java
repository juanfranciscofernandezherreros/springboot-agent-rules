package com.acme.orders.exception;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
        AppErrorMessage error = exception.getError();
        log.warn("[ORDER] - ACTION: handleAppException: error: {}", error.name());
        ErrorResponse response = new ErrorResponse(error.name(), error.getMessage(), Instant.now());

        return ResponseEntity.status(error.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        log.warn("[ORDER] - ACTION: handleValidationException");
        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", "Request validation failed", Instant.now());

        return ResponseEntity.badRequest().body(response);
    }
}
