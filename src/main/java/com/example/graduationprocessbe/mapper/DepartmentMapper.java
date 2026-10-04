package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.response.DepartmentResponse;
import com.example.graduationprocessbe.entity.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentResponse toResponse(Department department);
}
