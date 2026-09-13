package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.LoginRequest;
import com.example.graduationprocessbe.dto.response.LoginResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        response
                ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse response = authService.register(createUserRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        response
                ));
    }
}
