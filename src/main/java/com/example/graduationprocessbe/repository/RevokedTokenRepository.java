package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
    void deleteByExpiresAtBefore(Instant instant);
}
