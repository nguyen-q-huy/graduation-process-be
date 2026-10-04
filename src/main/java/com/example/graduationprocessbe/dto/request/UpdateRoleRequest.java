package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {

    @Size(max = 50, message = "Role code must not exceed 50 characters")
    private String roleCode;

    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String roleName;
}
