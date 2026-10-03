package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.DepartmentResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.entity.Department;
import com.example.graduationprocessbe.entity.Role;
import com.example.graduationprocessbe.entity.Security;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.entity.UserRole;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.UserMapper;
import com.example.graduationprocessbe.repository.DepartmentRepository;
import com.example.graduationprocessbe.repository.RoleRepository;
import com.example.graduationprocessbe.repository.SecurityRepository;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import com.example.graduationprocessbe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::enrichUserResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }

        User user = userMapper.toEntity(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        if (request.getUserType() != null && !request.getUserType().isBlank()) {
            user.setUserType(request.getUserType().trim().toUpperCase());
        }

        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            Department department = departmentRepository.findById(request.getDepartmentId()).orElse(null);
            user.setDepartment(department);
        }

        User savedUser = userRepository.save(user);

        // Create Security row
        Security security = new Security();
        security.setUserId(savedUser.getId());
        security.setUsername(request.getUsername());
        security.setPasswordHash(encodedPassword);
        securityRepository.save(security);

        // Assign initial role if provided
        if (request.getRoleId() != null && !request.getRoleId().isBlank()) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseGet(() -> roleRepository.findByRoleCode(request.getRoleId()).orElse(null));
            if (role != null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(savedUser.getId());
                userRole.setRoleId(role.getId());
                userRoleRepository.save(userRole);
            }
        }

        return enrichUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void assignRoleToUser(String userId, String roleId, String thesisRoundId) {
        if (!userRepository.existsById(userId)) {
            throw new ApplicationException(ResponseDetails.NOT_FOUND);
        }

        Role role = roleRepository.findById(roleId)
                .orElseGet(() -> roleRepository.findByRoleCode(roleId)
                        .orElseThrow(() -> new ApplicationException(ResponseDetails.NOT_FOUND)));

        boolean exists = userRoleRepository.existsByUserIdAndRoleId(userId, role.getId());
        if (!exists) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setThesisRoundId(thesisRoundId);
            userRoleRepository.save(userRole);
        }
    }

    @Override
    @Transactional
    public void removeRoleFromUser(String userId, String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseGet(() -> roleRepository.findByRoleCode(roleId).orElse(null));
        String actualRoleId = role != null ? role.getId() : roleId;
        userRoleRepository.deleteByUserIdAndRoleId(userId, actualRoleId);
    }

    private UserResponse enrichUserResponse(User user) {
        UserResponse response = userMapper.toResponse(user);
        List<String> roles = userRoleRepository.findRoleCodesByUserIdAndRoundId(user.getId(), null);
        Set<String> permissions = userRoleRepository.findPermissionCodesByUserIdAndRoundId(user.getId(), null);

        response.setRoles(roles);
        response.setRole(!roles.isEmpty() ? roles.get(0) : null);
        response.setPermissions(permissions);

        if (user.getDepartment() != null) {
            DepartmentResponse dept = new DepartmentResponse();
            dept.setId(user.getDepartment().getId());
            dept.setDeptCode(user.getDepartment().getDeptCode());
            dept.setDeptName(user.getDepartment().getDeptName());
            response.setDepartment(dept);
            response.setDepartmentName(user.getDepartment().getDeptName());
        }

        return response;
    }
}
