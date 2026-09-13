package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateThesisRequest {

    @NotBlank(message = "Thesis title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Thesis description is required")
    private String description;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Lecturer ID is required")
    private String lecturerId;

    @NotBlank(message = "Phase ID is required")
    private String phaseId;
}
