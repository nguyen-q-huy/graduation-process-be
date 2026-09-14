package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.LoginRequest;
import com.example.graduationprocessbe.dto.response.LoginResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse register(CreateUserRequest createUserRequest);
}
