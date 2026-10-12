package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.entity.Thesis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ThesisMapper {

    @Mapping(source = "titleEn", target = "titleEn")
    ThesisResponse toResponse(Thesis thesis);
}
