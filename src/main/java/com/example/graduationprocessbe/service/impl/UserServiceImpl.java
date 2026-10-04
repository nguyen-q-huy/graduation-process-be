package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.UpdateUserRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.entity.UserRole;
import com.example.graduationprocessbe.entity.UserRoleId;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResourceNotFoundException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.RoleMapper;
import com.example.graduationprocessbe.mapper.UserMapper;
import com.example.graduationprocessbe.repository.DepartmentRepository;
import com.example.graduationprocessbe.repository.MemberRepository;
import com.example.graduationprocessbe.repository.RoleRepository;
import com.example.graduationprocessbe.repository.SecurityRepository;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import com.example.graduationprocessbe.service.CurrentUserService;
import com.example.graduationprocessbe.service.UserService;
import com.example.graduationprocessbe.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Set<String> SORT_FIELDS = Set.of("email", "fullName", "status", "createdDate");

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRoleRepository userRoleRepository;
    private final MemberRepository memberRepository;
    private final SecurityRepository securityRepository;
    private final CurrentUserService currentUserService;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(String id) {
        return userMapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String keyword, String status, String departmentId,
                                             int page, int size, String sortBy, String direction) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (keyword != null && !keyword.isBlank()) {
            String pattern = PageUtil.likePattern(keyword);
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), pattern, '\\'),
                    cb.like(cb.lower(root.get("fullName")), pattern, '\\')));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (departmentId != null && !departmentId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }
        Page<User> result = userRepository.findAll(spec,
                PageUtil.of(page, size, sortBy, direction, SORT_FIELDS, "createdDate"));
        return PageResponse.from(result, userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(String id, UpdateUserRequest request) {
        User user = find(id);
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new ApplicationException(ResponseDetails.DATA_EXISTED);
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getDepartmentId() != null) {
            user.setDepartment(departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + request.getDepartmentId())));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(String id) {
        User user = find(id);
        memberRepository.deleteByUserId(id);
        userRoleRepository.deleteByUserId(id);
        securityRepository.findById(id).ifPresent(securityRepository::delete);
        userRepository.delete(user);
        userRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles(String userId) {
        find(userId);
        return rolesOf(userId);
    }

    @Override
    @Transactional
    public List<RoleResponse> assignRole(String userId, String roleId) {
        find(userId);
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        if (userRoleRepository.existsById(new UserRoleId(userId, roleId))) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }
        userRoleRepository.save(new UserRole(userId, roleId));
        return rolesOf(userId);
    }

    @Override
    @Transactional
    public List<RoleResponse> removeRole(String userId, String roleId) {
        UserRoleId key = new UserRoleId(userId, roleId);
        if (!userRoleRepository.existsById(key)) {
            throw new ResourceNotFoundException("User " + userId + " does not have role " + roleId);
        }
        userRoleRepository.deleteById(key);
        return rolesOf(userId);
    }

    private List<RoleResponse> rolesOf(String userId) {
        List<String> roleIds = userRoleRepository.findByUserId(userId).stream().map(UserRole::getRoleId).toList();
        return roleRepository.findAllById(roleIds).stream().map(roleMapper::toResponse).toList();
    }

    private User find(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
