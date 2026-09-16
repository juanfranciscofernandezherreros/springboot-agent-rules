package com.example.tasks.common.exception;

import com.example.tasks.common.exception.ErrorResponse.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception, HttpServletRequest request) {
        AppErrorMessage error = exception.getError();
        ErrorResponse response = createResponse(error, request.getRequestURI(), List.of());
        log.warn("[API] - ERROR: {}: path: {}", error.getCode(), request.getRequestURI());

        return ResponseEntity.status(error.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        AppErrorMessage error = AppErrorMessage.VALIDATION_ERROR;
        ErrorResponse response = createResponse(error, request.getRequestURI(), violations);
        log.warn("[API] - ERROR: {}: path: {}", error.getCode(), request.getRequestURI());

        return ResponseEntity.status(error.getStatus()).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<FieldViolation> violations = exception.getConstraintViolations().stream()
                .map(violation -> new FieldViolation(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        AppErrorMessage error = AppErrorMessage.VALIDATION_ERROR;
        ErrorResponse response = createResponse(error, request.getRequestURI(), violations);
        log.warn("[API] - ERROR: {}: path: {}", error.getCode(), request.getRequestURI());

        return ResponseEntity.status(error.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        AppErrorMessage error = AppErrorMessage.INTERNAL_ERROR;
        ErrorResponse response = createResponse(error, request.getRequestURI(), List.of());
        log.error("[API] - ERROR: {}: path: {}", error.getCode(), request.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ErrorResponse createResponse(AppErrorMessage error, String path, List<FieldViolation> violations) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(), error.getStatus().value(), error.getCode(), error.getMessage(), path, violations);

        return response;
    }
}
