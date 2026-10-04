package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);
    List<RolePermission> findByRoleId(String roleId);
    void deleteByRoleId(String roleId);
    void deleteByRoleIdAndPermissionId(String roleId, String permissionId);
}
