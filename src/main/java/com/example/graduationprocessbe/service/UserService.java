package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.UpdateUserRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getById(String id);

    /** User đang đăng nhập (theo JWT). */
    UserResponse getCurrentUser();

    PageResponse<UserResponse> search(String keyword, String status, String departmentId,
                                      int page, int size, String sortBy, String direction);

    UserResponse update(String id, UpdateUserRequest request);

    /** Xoá user cùng tài khoản đăng nhập, role và membership. Lỗi 409 nếu còn là SV/GVHD của đề tài. */
    void delete(String id);

    List<RoleResponse> getRoles(String userId);

    List<RoleResponse> assignRole(String userId, String roleId);

    List<RoleResponse> removeRole(String userId, String roleId);
}
