package com.example.graduationprocessbe.mapper;

import com.example.graduationprocessbe.dto.request.CreateMenuRequest;
import com.example.graduationprocessbe.dto.request.UpdateMenuRequest;
import com.example.graduationprocessbe.dto.response.MenuResponse;
import com.example.graduationprocessbe.entity.Menu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "children", ignore = true)
    MenuResponse toResponse(Menu menu);

    List<MenuResponse> toResponseList(List<Menu> menus);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Menu toEntity(CreateMenuRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateMenuRequest request, @MappingTarget Menu menu);
}
