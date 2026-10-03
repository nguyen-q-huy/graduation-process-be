package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreatePermissionRequest;
import com.example.graduationprocessbe.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    List<PermissionResponse> getAllPermissions();
    PermissionResponse createPermission(CreatePermissionRequest request);
}
