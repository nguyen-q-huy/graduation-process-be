package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateAuditLogRequest;
import com.example.graduationprocessbe.dto.response.AuditLogResponse;
import com.example.graduationprocessbe.entity.User;

import java.time.LocalDateTime;

public interface AuditLogService {

    AuditLogResponse create(CreateAuditLogRequest request);

    /** Ghi log từ code nội bộ (ví dụ khi complete task). actor có thể null. */
    void record(String processInstanceId, User actor, String actionName, String stepName, Object payload);

    AuditLogResponse getById(String id);

    PageResponse<AuditLogResponse> search(String processInstanceId, String actorId, String actionName,
                                          LocalDateTime from, LocalDateTime to,
                                          int page, int size, String sortBy, String direction);
}
