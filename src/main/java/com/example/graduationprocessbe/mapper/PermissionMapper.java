package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.request.CreatePermissionRequest;
import com.example.graduationprocessbe.dto.response.PermissionResponse;
import com.example.graduationprocessbe.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);

    List<PermissionResponse> toResponseList(List<Permission> permissions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Permission toEntity(CreatePermissionRequest request);
}
