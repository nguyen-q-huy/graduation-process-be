package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.request.UpdateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    RoleResponse getById(String id);

    PageResponse<RoleResponse> search(String keyword, int page, int size, String sortBy, String direction);

    RoleResponse update(String id, UpdateRoleRequest request);

    /** Xoá role và các liên kết user_roles của nó. */
    void delete(String id);
}
