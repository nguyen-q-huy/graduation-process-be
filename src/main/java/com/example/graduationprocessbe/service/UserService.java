package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse createUser(CreateUserRequest request);
    void assignRoleToUser(String userId, String roleId, String thesisRoundId);
    void removeRoleFromUser(String userId, String roleId);
}
