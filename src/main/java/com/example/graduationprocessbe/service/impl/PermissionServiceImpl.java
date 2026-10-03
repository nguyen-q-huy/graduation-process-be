package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreatePermissionRequest;
import com.example.graduationprocessbe.dto.response.PermissionResponse;
import com.example.graduationprocessbe.entity.Permission;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.PermissionMapper;
import com.example.graduationprocessbe.repository.PermissionRepository;
import com.example.graduationprocessbe.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionMapper.toResponseList(permissionRepository.findAll());
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }

        Permission permission = permissionMapper.toEntity(request);
        permission.setCode(permission.getCode().trim().toUpperCase());
        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toResponse(saved);
    }
}
