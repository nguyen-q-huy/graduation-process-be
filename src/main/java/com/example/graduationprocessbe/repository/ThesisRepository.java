package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Thesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ThesisRepository extends JpaRepository<Thesis, String>, JpaSpecificationExecutor<Thesis> {
    Optional<Thesis> findByProcessInstanceId(String processInstanceId);
}
