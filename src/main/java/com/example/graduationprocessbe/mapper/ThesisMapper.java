package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.entity.Thesis;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ThesisMapper {

    ThesisResponse toResponse(Thesis thesis);
}
