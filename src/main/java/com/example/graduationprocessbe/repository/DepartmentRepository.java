package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, String> {
}
