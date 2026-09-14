package com.example.graduationprocessbe.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThesisResponse {
    private String id;
    private String title;
    private String description;
    private UserResponse student;
    private UserResponse lecturer;
    private String phaseId;
    private String processInstanceId;
    private String currentStatus;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
