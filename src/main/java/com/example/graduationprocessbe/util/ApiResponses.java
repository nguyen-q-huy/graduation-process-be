package com.example.graduationprocessbe.util;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.exception.ResponseDetails;
import org.springframework.http.ResponseEntity;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ApiResponseWrapper<T>> ok(T data) {
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, data));
    }
}
