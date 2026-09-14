package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Email(message = "Email is invalid")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private String departmentId;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
}
