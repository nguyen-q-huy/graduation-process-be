package com.example.graduationprocessbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private String id;
    private String code;
    private String name;
    private String module;
    private String menuCode;
    private String action;
    private Boolean enabled;
}
