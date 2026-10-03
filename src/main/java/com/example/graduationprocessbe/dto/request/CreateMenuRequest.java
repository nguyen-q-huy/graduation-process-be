package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

    private String parentId;

    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    private String code;

    @NotBlank(message = "Label is required")
    @Size(max = 150, message = "Label must not exceed 150 characters")
    private String label;

    @Size(max = 100, message = "Icon must not exceed 100 characters")
    private String icon;

    @Size(max = 200, message = "Path must not exceed 200 characters")
    private String path;

    private Integer sortOrder = 0;

    @Size(max = 100, message = "Permission code must not exceed 100 characters")
    private String permissionCode;

    private Boolean active = true;
}
