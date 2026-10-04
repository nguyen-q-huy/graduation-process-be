package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Role toEntity(CreateRoleRequest request);
}
