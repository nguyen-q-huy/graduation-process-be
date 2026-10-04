package com.example.graduationprocessbe.repository;
import com.example.graduationprocessbe.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,String> {}
