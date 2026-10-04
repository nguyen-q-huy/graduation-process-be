package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.LoginRequest;
import com.example.graduationprocessbe.dto.response.DepartmentResponse;
import com.example.graduationprocessbe.dto.response.LoginResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.entity.Department;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.repository.DepartmentRepository;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import com.example.graduationprocessbe.service.EffectivePermissionService;
import com.example.graduationprocessbe.security.JwtTokenProvider;
import com.example.graduationprocessbe.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRoleRepository userRoleRepository;
    private final EffectivePermissionService effectivePermissions;
    private final PasswordEncoder passwordEncoder;
    private final com.example.graduationprocessbe.repository.RoleRepository roleRepository;

    @Override
    public void logout(String token) {
        jwtTokenProvider.revokeToken(token);
        SecurityContextHolder.clearContext();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // JWT contains ONLY userId
        String jwt = jwtTokenProvider.generateTokenByUserId(user.getId());

        UserResponse userResponse = mapUserToResponse(user);
        userResponse.setUsername(user.getUsername());

        return new LoginResponse(jwt, "Bearer", userResponse);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public UserResponse register(CreateUserRequest createUserRequest) {
        if (userRepository.existsByUsername(createUserRequest.getUsername().trim())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(createUserRequest.getEmail());
        user.setFullName(createUserRequest.getFullName());

        String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());

        user.setStatus("ACTIVE");
        user.setUserType("STUDENT");

        if (createUserRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(createUserRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartment(department);
        }

        user.setUsername(createUserRequest.getUsername().trim());
        user.setPasswordHash(encodedPassword);
        User savedUser = userRepository.save(user);

        // Public registration always creates a STUDENT, never a caller-supplied role.
        var membership = new com.example.graduationprocessbe.entity.UserRole();
        membership.setUserId(savedUser.getId());
        membership.setRoleId(roleRepository.findByRoleCode("STUDENT").orElseThrow().getId());
        userRoleRepository.save(membership);

        return mapUserToResponse(savedUser);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userId) {
        return mapUserToResponse(userRepository.findById(userId).orElseThrow());
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setStatus(user.getStatus());
        response.setUserType(user.getUserType());
        response.setCreatedDate(user.getCreatedDate());
        response.setLastModifiedDate(user.getLastModifiedDate());

        List<String> roles = userRoleRepository.findRoleCodesByUserIdAndRoundId(user.getId(), null);
        Set<String> permissions = effectivePermissions.codes(user.getId(),null);

        response.setRoles(roles);
        response.setRole(EffectivePermissionService.primaryRole(roles));
        response.setPermissions(permissions);

        if (user.getDepartment() != null) {
            DepartmentResponse deptResponse = new DepartmentResponse();
            deptResponse.setId(user.getDepartment().getId());
            deptResponse.setDeptCode(user.getDepartment().getDeptCode());
            deptResponse.setDeptName(user.getDepartment().getDeptName());
            response.setDepartment(deptResponse);
            response.setDepartmentName(user.getDepartment().getDeptName());
        }

        return response;
    }
}
