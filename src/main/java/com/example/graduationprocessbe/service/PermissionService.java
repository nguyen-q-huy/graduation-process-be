package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    List<PermissionResponse> getAllPermissions();
}
