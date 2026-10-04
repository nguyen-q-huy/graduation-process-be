package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.response.PermissionResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.PermissionService;
import com.example.graduationprocessbe.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_ROLES','VIEW_MENUS')")
    public ResponseEntity<ApiResponseWrapper<?>> getAllPermissions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module) {
        String keyword = PaginationUtils.normalize(search);
        String moduleFilter = PaginationUtils.normalize(module);
        List<PermissionResponse> filtered = permissionService.getAllPermissions().stream()
                .filter(permission -> keyword.isBlank()
                        || PaginationUtils.normalize(permission.getCode()).contains(keyword)
                        || PaginationUtils.normalize(permission.getName()).contains(keyword))
                .filter(permission -> moduleFilter.isBlank()
                        || "all".equals(moduleFilter)
                        || PaginationUtils.normalize(permission.getModule()).equals(moduleFilter))
                .toList();
        Object data = page == null && size == null ? filtered : PaginationUtils.page(filtered, page, size);
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, data));
    }

}
