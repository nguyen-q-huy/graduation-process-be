package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.request.UpdateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.entity.Role;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResourceNotFoundException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.RoleMapper;
import com.example.graduationprocessbe.repository.RoleRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import com.example.graduationprocessbe.service.RoleService;
import com.example.graduationprocessbe.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Set<String> SORT_FIELDS = Set.of("roleCode", "roleName", "createdDate");

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(String id) {
        return roleMapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> search(String keyword, int page, int size, String sortBy, String direction) {
        Specification<Role> spec = (root, query, cb) -> cb.conjunction();
        if (keyword != null && !keyword.isBlank()) {
            String pattern = PageUtil.likePattern(keyword);
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("roleCode")), pattern, '\\'),
                    cb.like(cb.lower(root.get("roleName")), pattern, '\\')));
        }
        Page<Role> result = roleRepository.findAll(spec,
                PageUtil.of(page, size, sortBy, direction, SORT_FIELDS, "createdDate"));
        return PageResponse.from(result, roleMapper::toResponse);
    }

    @Override
    @Transactional
    public RoleResponse update(String id, UpdateRoleRequest request) {
        Role role = find(id);
        if (request.getRoleCode() != null && !request.getRoleCode().equals(role.getRoleCode())) {
            if (roleRepository.existsByRoleCode(request.getRoleCode())) {
                throw new ApplicationException(ResponseDetails.DATA_EXISTED);
            }
            role.setRoleCode(request.getRoleCode());
        }
        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Role role = find(id);
        userRoleRepository.deleteByRoleId(id);
        roleRepository.delete(role);
    }

    private Role find(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }
}
