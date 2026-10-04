package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {
    boolean existsByRoleCode(String roleCode);
}
