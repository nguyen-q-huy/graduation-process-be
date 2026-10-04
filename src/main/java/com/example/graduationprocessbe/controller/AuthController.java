package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.LoginRequest;
import com.example.graduationprocessbe.dto.response.LoginResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.security.CustomUserDetails;
import com.example.graduationprocessbe.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponseWrapper<UserResponse> me(Authentication authentication) {
        var principal = (CustomUserDetails) authentication.getPrincipal();
        UserResponse response = authService.getCurrentUser(principal.getUserId());
        response.setRoles(principal.getRoles()); response.setPermissions(principal.getPermissions());
        response.setRole(com.example.graduationprocessbe.service.EffectivePermissionService.primaryRole(principal.getRoles()));
        return new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY,response);
    }

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
