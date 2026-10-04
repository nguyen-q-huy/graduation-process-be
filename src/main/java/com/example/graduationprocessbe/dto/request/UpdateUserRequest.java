package com.example.graduationprocessbe.dto.request;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class UpdateUserRequest {
    @NotBlank @Size(max=100) private String fullName;
    @NotBlank @Email @Size(max=120) private String email;
    @NotBlank @Pattern(regexp="ACTIVE|INACTIVE") private String status;
    @NotBlank @Pattern(regexp="ADMIN|LECTURER|STUDENT") private String userType;
}
