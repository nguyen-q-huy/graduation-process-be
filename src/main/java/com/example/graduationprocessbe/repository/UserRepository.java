package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
    java.util.List<User> findAll();
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
