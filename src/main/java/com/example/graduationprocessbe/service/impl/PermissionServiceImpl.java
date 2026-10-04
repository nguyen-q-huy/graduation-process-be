package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.response.PermissionResponse;
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

}
