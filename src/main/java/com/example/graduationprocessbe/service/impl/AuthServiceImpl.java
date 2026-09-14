package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.LoginRequest;
import com.example.graduationprocessbe.dto.response.DepartmentResponse;
import com.example.graduationprocessbe.dto.response.LoginResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.entity.Department;
import com.example.graduationprocessbe.entity.Security;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.repository.DepartmentRepository;
import com.example.graduationprocessbe.repository.SecurityRepository;
import com.example.graduationprocessbe.repository.UserRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        Security security = securityRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = security.getUser();
        UserResponse userResponse = mapUserToResponse(user);

        return new LoginResponse(jwt, "Bearer", userResponse);
    }

    @Override
    public UserResponse register(CreateUserRequest createUserRequest) {
        if (securityRepository.existsByUsername(createUserRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(createUserRequest.getEmail());
        user.setFullName(createUserRequest.getFullName());
        user.setStatus(createUserRequest.getStatus());

        if (createUserRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(createUserRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartment(department);
        }

        User savedUser = userRepository.save(user);

        Security security = new Security();
        security.setUsername(createUserRequest.getUsername());
        security.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        security.setUser(savedUser);
        securityRepository.save(security);

        return mapUserToResponse(savedUser);
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setStatus(user.getStatus());
        response.setCreatedDate(user.getCreatedDate());
        response.setLastModifiedDate(user.getLastModifiedDate());

        if (user.getDepartment() != null) {
            DepartmentResponse deptResponse = new DepartmentResponse();
            deptResponse.setId(user.getDepartment().getId());
            response.setDepartment(deptResponse);
        }

        return response;
    }
}
