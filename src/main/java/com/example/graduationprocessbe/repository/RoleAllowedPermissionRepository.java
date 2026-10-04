package com.example.graduationprocessbe.repository;
import com.example.graduationprocessbe.entity.RoleAllowedPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoleAllowedPermissionRepository extends JpaRepository<RoleAllowedPermission,String> {
    List<RoleAllowedPermission> findByRoleId(String roleId);
    List<RoleAllowedPermission> findByPermissionId(String permissionId);
    boolean existsByRoleIdAndPermissionId(String roleId,String permissionId);
}
