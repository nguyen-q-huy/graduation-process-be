package com.example.graduationprocessbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String id;
    private String roleCode;
    private String roleName;
    private Integer permissionCount;
    private List<String> permissionCodes;
    private List<String> permissionIds;
    private List<String> allowedPermissionIds;
    private Long permissionsVersion;
}
