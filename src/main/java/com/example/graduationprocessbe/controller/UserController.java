package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        userService.createUser(request)));
    }
}
