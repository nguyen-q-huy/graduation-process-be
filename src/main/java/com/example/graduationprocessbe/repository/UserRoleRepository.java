package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    boolean existsByUserIdAndRoleId(String userId, String roleId);

    @Query("""
            select r.roleCode
            from UserRole ur
            join Role r on r.id = ur.roleId
            where ur.userId = :userId
            """)
    Optional<String> findRoleCodeByUserId(@Param("userId") String userId);

    @Query("""
            select distinct r.roleCode
            from UserRole ur
            join Role r on r.id = ur.roleId
            where ur.userId = :userId
              and (ur.thesisRoundId is null or :roundId is null or ur.thesisRoundId = :roundId)
            """)
    List<String> findRoleCodesByUserIdAndRoundId(
            @Param("userId") String userId,
            @Param("roundId") String roundId);

    @Query("""
            select distinct p.code
            from UserRole ur
            join RolePermission rp on rp.roleId = ur.roleId
            join Permission p on p.id = rp.permissionId
            where ur.userId = :userId
              and (ur.thesisRoundId is null or :roundId is null or ur.thesisRoundId = :roundId)
            """)
    Set<String> findPermissionCodesByUserIdAndRoundId(
            @Param("userId") String userId,
            @Param("roundId") String roundId);

    List<UserRole> findByUserId(String userId);

    void deleteByUserIdAndRoleId(String userId, String roleId);

    void deleteByUserId(String userId);
}
