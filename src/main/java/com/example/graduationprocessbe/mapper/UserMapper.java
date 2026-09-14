package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "department", ignore = true)
    User toEntity(CreateUserRequest request);
}
