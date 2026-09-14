package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMemberRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Thesis ID is required")
    private String thesisId;
}
