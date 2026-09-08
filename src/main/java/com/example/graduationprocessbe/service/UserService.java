package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
}
