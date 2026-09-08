package com.example.graduationprocessbe.dto;

import com.example.graduationprocessbe.exception.ResponseDetails;
import lombok.Data;

@Data
public class ApiResponseWrapper<T> {

    private String status;

    private String code;

    private String message;

    private T data;

    // Constructor using ResponseDetails
    public ApiResponseWrapper(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Constructor using ResponseDetails
    public ApiResponseWrapper(ResponseDetails responseDetails, T data) {

        this.status = responseDetails.getHttpStatus().is2xxSuccessful() ? "SUCCESS" : "FAILURE";
        this.code = responseDetails.getCode();
        this.message = responseDetails.getMessage();
        this.data = data;
    }

    // Constructor using ResponseDetails
    public ApiResponseWrapper(String status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
