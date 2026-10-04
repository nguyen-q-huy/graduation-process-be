package com.example.graduationprocessbe.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String role;
    private List<String> roles;
    private Set<String> permissions;
    private String userType;
    private String departmentName;
    private String status;
    private DepartmentResponse department;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
