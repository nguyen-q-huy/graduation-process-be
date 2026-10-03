package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityRepository extends JpaRepository<Security, String> {
    boolean existsByUsername(String username);

    Optional<Security> findByUsername(String username);

    Optional<Security> findByUserId(String userId);
}
