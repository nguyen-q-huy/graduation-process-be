package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDepartmentRequest {

    @Size(max = 50, message = "Department code must not exceed 50 characters")
    private String deptCode;

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String deptName;
}
