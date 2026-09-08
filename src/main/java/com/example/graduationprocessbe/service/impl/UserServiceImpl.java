package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.UserMapper;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }

        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }
}
