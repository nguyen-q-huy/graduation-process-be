package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreateMenuRequest;
import com.example.graduationprocessbe.dto.request.UpdateMenuRequest;
import com.example.graduationprocessbe.dto.response.MenuResponse;
import java.util.List;

public interface MenuService {
    List<MenuResponse> getUserMenus(String userId, String roundId);
    List<MenuResponse> getMasterMenuTree();
    MenuResponse createMenu(CreateMenuRequest request);
    MenuResponse updateMenu(String id, UpdateMenuRequest request);
    void deleteMenu(String id);
}
