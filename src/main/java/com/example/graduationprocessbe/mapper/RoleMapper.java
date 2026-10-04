package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);
}
