package com.example.graduationprocessbe.service;
import com.example.graduationprocessbe.entity.AuditLog;
import com.example.graduationprocessbe.repository.AuditLogRepository;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RbacAuditService {
    private final AuditLogRepository logs;
    private final UserRepository users;
    public void record(String action,String target,Object before,Object after) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) return;
        AuditLog log = new AuditLog();
        log.setActor(users.getReferenceById(principal.getUserId()));
        log.setActionName(action); log.setStepName("RBAC"); log.setExecutionTime(LocalDateTime.now());
        log.setPayload(Map.of("target",target,"before",before,"after",after));
        logs.save(log);
    }
}
