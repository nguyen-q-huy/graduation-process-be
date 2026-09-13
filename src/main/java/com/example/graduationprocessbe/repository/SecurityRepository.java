package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityRepository extends JpaRepository<Security, String> {
    boolean existsByUsername(String username);

    Optional<Security> findByUsername(String username);
}
