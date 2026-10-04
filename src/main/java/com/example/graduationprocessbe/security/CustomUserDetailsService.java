package com.example.graduationprocessbe.security;

import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import com.example.graduationprocessbe.service.EffectivePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final EffectivePermissionService effectivePermissions;

    /**
     * Load user by userId and active thesisRoundId.
     * Queries DB on EVERY incoming request to fetch fresh roles and permissions.
     */
    public CustomUserDetails loadUserByUserId(String userId, String roundId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        String username = user.getUsername();
        String password = user.getPasswordHash();

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Fresh Roles from DB
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserIdAndRoundId(userId, roundId);
        for (String role : roleCodes) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // 2. Fresh Permissions from DB
        Set<String> permissionCodes = effectivePermissions.codes(userId,roundId);
        for (String perm : permissionCodes) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }

        return new CustomUserDetails(
                userId,
                username,
                password,
                user.getFullName(),
                user.getEmail(),
                user.getUserType(),
                user.getStatus(),
                roleCodes,
                permissionCodes,
                authorities
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return loadUserByUserId(user.getId(), null);
    }
}
