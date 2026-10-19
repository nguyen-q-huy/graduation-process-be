package com.example.graduationprocessbe.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuResponse {
    private String id;
    private String parentId;
    private String code;
    private String label;
    private String icon;
    private String path;
    private Integer sortOrder;
    private String permissionCode;
    private Boolean active;
    /** Shown but not clickable (e.g. a student's "in progress" page before the supervisor confirms guidance). */
    private Boolean disabled;
    @Builder.Default
    private List<MenuResponse> children = new ArrayList<>();
}
