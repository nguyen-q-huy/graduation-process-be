package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.response.PermissionResponse;
import com.example.graduationprocessbe.entity.Permission;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);

    List<PermissionResponse> toResponseList(List<Permission> permissions);

}
