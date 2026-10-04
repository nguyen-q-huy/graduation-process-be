package com.example.graduationprocessbe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionsRequest {
    @jakarta.validation.constraints.NotNull
    private List<String> permissionIds;
    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.PositiveOrZero
    private Long expectedVersion;
}
