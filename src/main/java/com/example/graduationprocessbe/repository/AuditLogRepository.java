package com.example.graduationprocessbe.repository;
import com.example.graduationprocessbe.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface AuditLogRepository extends JpaRepository<AuditLog,String>, JpaSpecificationExecutor<AuditLog> {}
