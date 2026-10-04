package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.UserRole;
import com.example.graduationprocessbe.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(String userId);

    void deleteByUserId(String userId);

    void deleteByRoleId(String roleId);
}
