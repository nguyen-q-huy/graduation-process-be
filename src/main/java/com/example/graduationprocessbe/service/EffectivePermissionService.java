package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.entity.Permission;
import com.example.graduationprocessbe.repository.PermissionRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EffectivePermissionService {
    private final UserRoleRepository memberships;
    private final PermissionRepository permissions;

    public boolean isGlobalAdmin(String userId) {
        return memberships.findRoleCodesByUserIdAndRoundId(userId, null).contains("ADMIN");
    }

    public Set<String> codes(String userId, String roundId) {
        if (isGlobalAdmin(userId)) return permissions.findAll().stream()
            .filter(p -> Boolean.TRUE.equals(p.getEnabled())).map(Permission::getCode)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return memberships.findPermissionCodesByUserIdAndRoundId(userId, roundId);
    }

    public static String primaryRole(List<String> roles) {
        return List.of("ADMIN","FACULTY_STAFF","COMMITTEE","LECTURER","STUDENT").stream()
            .filter(roles::contains).findFirst().orElse(roles.isEmpty() ? null : roles.getFirst());
    }
}
