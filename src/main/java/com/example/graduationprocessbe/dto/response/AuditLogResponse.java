package com.example.graduationprocessbe.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {
    private String id;
    private String processInstanceId;
    private UserResponse actor;
    private String actionName;
    private String stepName;
    private String payload;
    private LocalDateTime executionTime;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
