package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Lấy user đang đăng nhập từ principal (CustomUserDetails) do JwtAuthenticationFilter đặt. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            return Optional.empty();
        }
        return userRepository.findById(details.getUserId());
    }
}
