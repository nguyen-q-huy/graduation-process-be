package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    boolean existsByUserIdAndRoleId(String userId, String roleId);

    @Query("""
            select distinct r.roleCode
            from UserRole ur
            join Role r on r.id = ur.roleId
            where ur.userId = :userId
              and (ur.thesisRoundId is null or ur.thesisRoundId = :roundId)
            """)
    List<String> findRoleCodesByUserIdAndRoundId(
            @Param("userId") String userId,
            @Param("roundId") String roundId);

    @Query("""
            select distinct p.code
            from UserRole ur
            join RolePermission rp on rp.roleId = ur.roleId
            join Permission p on p.id = rp.permissionId
            join RoleAllowedPermission ap on ap.roleId = rp.roleId and ap.permissionId = p.id
            where ur.userId = :userId
              and p.enabled = true
              and (ur.thesisRoundId is null or ur.thesisRoundId = :roundId)
            """)
    Set<String> findPermissionCodesByUserIdAndRoundId(
            @Param("userId") String userId,
            @Param("roundId") String roundId);


    @Query("select count(ur) from UserRole ur join Role r on r.id=ur.roleId join User u on u.id=ur.userId where r.roleCode='ADMIN' and ur.thesisRoundId is null and u.status='ACTIVE'")
    long countActiveGlobalAdmins();
    boolean existsByRoleId(String roleId);
    List<UserRole> findByUserId(String userId);


}
