package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.AssignPermissionsRequest;
import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.RoleService;
import com.example.graduationprocessbe.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_USERS','VIEW_ROLES','VIEW_MENUS')")
    public ResponseEntity<ApiResponseWrapper<?>> getAllRoles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {
        String keyword = PaginationUtils.normalize(search);
        List<RoleResponse> filtered = roleService.getAllRoles().stream()
                .filter(role -> keyword.isBlank()
                        || PaginationUtils.normalize(role.getRoleCode()).contains(keyword)
                        || PaginationUtils.normalize(role.getRoleName()).contains(keyword))
                .toList();
        Object data = page == null && size == null ? filtered : PaginationUtils.page(filtered, page, size);
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, data));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_ROLES') and hasAuthority('ROLES_CREATE')")
    public ResponseEntity<ApiResponseWrapper<RoleResponse>> createRole(
            @RequestBody @Valid CreateRoleRequest request) {
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        roleService.createRole(request)));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_ROLES') and hasAuthority('ROLES_UPDATE')")
    public ApiResponseWrapper<RoleResponse> updateRole(@PathVariable String roleId,@Valid @RequestBody CreateRoleRequest request) {
        return new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY,roleService.updateRole(roleId,request));
    }
    @org.springframework.web.bind.annotation.DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_ROLES') and hasAuthority('ROLES_DELETE')")
    public ApiResponseWrapper<Void> deleteRole(@PathVariable String roleId) {
        roleService.deleteRole(roleId); return new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY,null);
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('VIEW_ROLES')")
    public ResponseEntity<ApiResponseWrapper<List<String>>> getRolePermissionIds(
            @PathVariable String roleId) {
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        roleService.getRolePermissionIds(roleId)));
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('VIEW_ROLES') and hasAuthority('ROLES_UPDATE')")
    public ResponseEntity<ApiResponseWrapper<Void>> updateRolePermissions(
            @PathVariable String roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.updateRolePermissions(roleId, request.getPermissionIds(), request.getExpectedVersion());
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, null));
    }
}
