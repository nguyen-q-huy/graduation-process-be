package com.example.graduationprocessbe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseDetails {

    API_SUCCESSFULLY("API_SUCCESSFULLY", "Success", HttpStatus.OK),
    DATA_EXISTED("DATA_EXISTED", "Data already exists", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
