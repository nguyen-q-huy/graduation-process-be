package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
    RoleResponse createRole(CreateRoleRequest request);
    RoleResponse updateRole(String id,CreateRoleRequest request);
    void deleteRole(String id);
    List<String> getRolePermissionIds(String roleId);
    void updateRolePermissions(String roleId, List<String> permissionIds, long expectedVersion);
}
