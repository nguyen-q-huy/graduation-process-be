package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.entity.*;
import com.example.graduationprocessbe.repository.*;
import com.example.graduationprocessbe.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleMembershipService {
    private final RoleRepository roles;
    private final UserRepository users;
    private final UserRoleRepository memberships;
    private final ThesisRoundRepository rounds;
    private final RbacAuditService audit;
    private final RolePermissionRepository rolePermissions;
    private final PermissionRepository permissions;

    public void add(String userId, String roleId, String roundId) {
        Role resolved = roles.findById(roleId).orElseGet(() -> roles.findByRoleCode(roleId)
            .orElseThrow(() -> bad("Vai trò không tồn tại")));
        Role role = roles.lockById(resolved.getId()).orElseThrow();
        User user = users.findById(userId).orElseThrow(() -> bad("Tài khoản không tồn tại"));
        checkDelegation(role);
        if (!compatible(user.getUserType(),role.getRoleCode())) throw bad("Vai trò không phù hợp loại hồ sơ");
        if ("ADMIN".equals(role.getRoleCode())) {
            if (roundId != null) throw bad("ADMIN chỉ được gán toàn hệ thống");
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())))
                throw new ApplicationException("FORBIDDEN", "Chỉ ADMIN được gán ADMIN", HttpStatus.FORBIDDEN);
        }
        if (roundId != null && !rounds.findById(roundId).map(r -> Boolean.TRUE.equals(r.getActive())).orElse(false))
            throw bad("Đợt không tồn tại hoặc đã ngừng hoạt động");
        if (Set.of("LECTURER","FACULTY_STAFF","COMMITTEE").contains(role.getRoleCode()) && !"LECTURER".equals(user.getUserType()))
            throw bad("Vai trò Giảng viên/Khoa/Hội đồng chỉ dành cho giảng viên");
        if (Set.of("FACULTY_STAFF","COMMITTEE").contains(role.getRoleCode())) {
            if (!"LECTURER".equals(user.getUserType())) throw bad("Vai trò Khoa/Hội đồng chỉ dành cho giảng viên");
            add(userId, roles.findByRoleCode("LECTURER").orElseThrow().getId(), roundId);
        }
        if (memberships.findByUserId(userId).stream().anyMatch(m -> role.getId().equals(m.getRoleId())
            && Objects.equals(roundId,m.getThesisRoundId()))) return;
        UserRole membership = new UserRole();
        membership.setUserId(userId); membership.setRoleId(role.getId()); membership.setThesisRoundId(roundId);
        memberships.save(membership);
        audit.record("ROLE_ASSIGNED",userId,List.of(),List.of(role.getRoleCode(),roundId == null ? "GLOBAL" : roundId));
    }

    public void remove(String id) {
        UserRole membership = memberships.findById(id).orElseThrow(() -> bad("Thành viên không tồn tại"));
        Role role = roles.lockById(membership.getRoleId()).orElseThrow();
        checkDelegation(role);
        if ("ADMIN".equals(role.getRoleCode())) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())))
                throw new ApplicationException("FORBIDDEN", "Chỉ ADMIN được bỏ ADMIN", HttpStatus.FORBIDDEN);
            if (memberships.countActiveGlobalAdmins() <= 1 && users.findById(membership.getUserId())
                .map(u -> "ACTIVE".equals(u.getStatus())).orElse(false)) throw bad("Cần giữ ít nhất một ADMIN hoạt động toàn hệ thống");
        }
        if ("LECTURER".equals(role.getRoleCode())) {
            Set<String> supplementary = roles.findAll().stream()
                .filter(r -> Set.of("FACULTY_STAFF","COMMITTEE").contains(r.getRoleCode())).map(Role::getId)
                .collect(java.util.stream.Collectors.toSet());
            if (memberships.findByUserId(membership.getUserId()).stream().anyMatch(m -> supplementary.contains(m.getRoleId())
                && (membership.getThesisRoundId() == null || Objects.equals(membership.getThesisRoundId(),m.getThesisRoundId()))))
                throw bad("Bỏ vai trò Khoa/Hội đồng trước khi bỏ vai trò Giảng viên");
        }
        if (Set.of("ADMIN","STUDENT","LECTURER").contains(role.getRoleCode()) && membership.getThesisRoundId()==null)
            throw bad("Vai trò nền gắn với loại hồ sơ; khóa tài khoản nếu cần thu hồi truy cập");
        memberships.delete(membership);
        audit.record("ROLE_REMOVED",membership.getUserId(),List.of(role.getRoleCode()),List.of());
    }

    public void removeAll(String userId,String roleId) {
        Role role = roles.findById(roleId).orElseGet(() -> roles.findByRoleCode(roleId).orElseThrow(() -> bad("Vai trò không tồn tại")));
        memberships.findByUserId(userId).stream().filter(m -> role.getId().equals(m.getRoleId()))
            .map(UserRole::getId).toList().forEach(this::remove);
    }
    private void checkDelegation(Role role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) return;
        Set<String> own = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        var ids = rolePermissions.findByRoleId(role.getId()).stream().map(RolePermission::getPermissionId).toList();
        if (permissions.findAllById(ids).stream().filter(p -> Boolean.TRUE.equals(p.getEnabled())).anyMatch(p -> !own.contains(p.getCode())))
            throw new ApplicationException("FORBIDDEN","Vai trò có quyền vượt quá phạm vi bạn được quản lý",HttpStatus.FORBIDDEN);
    }
    public static boolean compatible(String type,String role) {
        return switch(type) {
            case "STUDENT" -> "STUDENT".equals(role);
            case "ADMIN" -> "ADMIN".equals(role);
            case "LECTURER" -> Set.of("LECTURER","FACULTY_STAFF","COMMITTEE").contains(role);
            default -> false;
        };
    }
    private ApplicationException bad(String message) { return new ApplicationException("INVALID_MEMBERSHIP",message,HttpStatus.BAD_REQUEST); }
}
