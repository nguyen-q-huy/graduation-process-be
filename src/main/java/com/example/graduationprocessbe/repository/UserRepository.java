package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
