package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Thesis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThesisRepository extends JpaRepository<Thesis, String> {
    Optional<Thesis> findByProcessInstanceId(String processInstanceId);
}
