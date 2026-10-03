package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.entity.Permission;
import com.example.graduationprocessbe.entity.Role;
import com.example.graduationprocessbe.entity.RolePermission;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.RoleMapper;
import com.example.graduationprocessbe.repository.PermissionRepository;
import com.example.graduationprocessbe.repository.RolePermissionRepository;
import com.example.graduationprocessbe.repository.RoleRepository;
import com.example.graduationprocessbe.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        Map<String, Permission> permMap = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getId, p -> p, (a, b) -> a));

        List<RolePermission> allRolePerms = rolePermissionRepository.findAll();
        Map<String, List<RolePermission>> rolePermsByRoleId = allRolePerms.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId));

        return roles.stream().map(role -> {
            List<RolePermission> perms = rolePermsByRoleId.getOrDefault(role.getId(), List.of());
            List<String> permIds = perms.stream().map(RolePermission::getPermissionId).toList();
            List<String> permCodes = permIds.stream()
                    .map(id -> permMap.get(id) != null ? permMap.get(id).getCode() : null)
                    .filter(c -> c != null)
                    .toList();

            return new RoleResponse(
                    role.getId(),
                    role.getRoleCode(),
                    role.getRoleName(),
                    permIds.size(),
                    permCodes,
                    permIds
            );
        }).toList();
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }

        Role role = roleMapper.toEntity(request);
        role.setRoleCode(role.getRoleCode().trim().toUpperCase());
        Role saved = roleRepository.save(role);
        RoleResponse res = roleMapper.toResponse(saved);
        res.setPermissionCount(0);
        res.setPermissionCodes(List.of());
        res.setPermissionIds(List.of());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRolePermissionIds(String roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(RolePermission::getPermissionId)
                .toList();
    }

    @Override
    @Transactional
    public void updateRolePermissions(String roleId, List<String> permissionIds) {
        if (!roleRepository.existsById(roleId)) {
            throw new ApplicationException(ResponseDetails.NOT_FOUND);
        }

        rolePermissionRepository.deleteByRoleId(roleId);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> newMappings = new ArrayList<>();
            for (String permId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                newMappings.add(rp);
            }
            rolePermissionRepository.saveAll(newMappings);
        }
    }
}
