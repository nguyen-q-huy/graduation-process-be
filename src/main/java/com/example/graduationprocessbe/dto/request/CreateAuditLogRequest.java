package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAuditLogRequest {

    @NotBlank(message = "Process instance ID is required")
    private String processInstanceId;

    @NotBlank(message = "Actor ID is required")
    private String actorId;

    @NotBlank(message = "Action name is required")
    private String actionName;

    @NotBlank(message = "Step name is required")
    private String stepName;

    private String payload;
}
