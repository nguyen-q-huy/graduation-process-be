package com.example.graduationprocessbe.exception;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseWrapper<String>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(
                "FAILURE",
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                null
        );
        
        log.error("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponseWrapper<List<String>>> handleValidationExceptions(Exception ex) {
        List<String> errors;
        
        if (ex instanceof MethodArgumentNotValidException methodEx) {
            errors = methodEx.getBindingResult()
                    .getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else if (ex instanceof ConstraintViolationException constraintEx) {
            errors = constraintEx.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            errors = List.of("Unknown validation error");
        }

        String errorMessage = errors.isEmpty() ? "Validation failed" : errors.get(0);
        
        ApiResponseWrapper<List<String>> response = new ApiResponseWrapper<>(
                "FAILURE",
                "VALIDATION_ERROR",
                errorMessage,
                errors
        );

        log.error("Validation error: {} - Errors: {}", ex.getMessage(), errors, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponseWrapper<String>> handleApplicationException(
            ApplicationException ex,
            HttpServletRequest request) {
        
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(
                "FAILURE",
                ex.getErrorCode(),
                ex.getMessage(),
                null
        );
        
        log.error("Application error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<String>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(
                "FAILURE",
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                ex.getMessage()
        );
        
        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
