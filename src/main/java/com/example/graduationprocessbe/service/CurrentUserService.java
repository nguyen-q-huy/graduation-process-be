package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.entity.Security;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Lấy user đang đăng nhập từ JWT (subject = username trong bảng security). */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final SecurityRepository securityRepository;

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return securityRepository.findByUsername(authentication.getName()).map(Security::getUser);
    }
}
