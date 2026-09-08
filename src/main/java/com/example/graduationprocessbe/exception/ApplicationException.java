package com.example.graduationprocessbe.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus status;

    public ApplicationException(ResponseDetails responseDetails) {
        super(responseDetails.getMessage());
        this.errorCode = responseDetails.getCode();
        this.status = responseDetails.getHttpStatus();
    }

    public ApplicationException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
