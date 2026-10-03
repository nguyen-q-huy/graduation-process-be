package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateMenuRequest;
import com.example.graduationprocessbe.dto.request.UpdateMenuRequest;
import com.example.graduationprocessbe.dto.response.MenuResponse;
import com.example.graduationprocessbe.entity.Menu;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.MenuMapper;
import com.example.graduationprocessbe.repository.MenuRepository;
import com.example.graduationprocessbe.repository.UserRoleRepository;
import com.example.graduationprocessbe.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final UserRoleRepository userRoleRepository;
    private final MenuMapper menuMapper;

    @Override
    public MenuResponse createMenu(CreateMenuRequest request) {
        if (menuRepository.existsByCode(request.getCode())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }

        Menu menu = menuMapper.toEntity(request);
        Menu saved = menuRepository.save(menu);
        return menuMapper.toResponse(saved);
    }

    @Override
    public MenuResponse updateMenu(String id, UpdateMenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ResponseDetails.NOT_FOUND));

        menuMapper.updateEntityFromRequest(request, menu);
        Menu saved = menuRepository.save(menu);
        return menuMapper.toResponse(saved);
    }

    @Override
    public void deleteMenu(String id) {
        if (!menuRepository.existsById(id)) {
            throw new ApplicationException(ResponseDetails.NOT_FOUND);
        }
        menuRepository.deleteById(id);
    }

    @Override
    public List<MenuResponse> getUserMenus(String userId, String roundId) {
        List<String> roles = userRoleRepository.findRoleCodesByUserIdAndRoundId(userId, roundId);
        boolean isAdmin = roles.contains("ADMIN");

        Set<String> userPermissions = userRoleRepository.findPermissionCodesByUserIdAndRoundId(userId, roundId);

        List<Menu> allActiveMenus = menuRepository.findByActiveTrueOrderBySortOrderAsc();

        // Build hierarchical tree
        List<MenuResponse> rootNodes = buildMenuTree(allActiveMenus);

        // If admin, all menus visible
        if (isAdmin) {
            return rootNodes;
        }

        // Filter tree recursively based on permissionCode:
        // A leaf node is visible if permissionCode is null/empty or in userPermissions
        // A parent node is visible if it has at least one visible child!
        return filterByPermissions(rootNodes, userPermissions);
    }

    @Override
    public List<MenuResponse> getMasterMenuTree() {
        List<Menu> allActiveMenus = menuRepository.findByActiveTrueOrderBySortOrderAsc();
        return buildMenuTree(allActiveMenus);
    }

    private List<MenuResponse> buildMenuTree(List<Menu> menus) {
        Map<String, MenuResponse> responseMap = new LinkedHashMap<>();
        for (Menu m : menus) {
            responseMap.put(m.getId(), MenuResponse.builder()
                    .id(m.getId())
                    .parentId(m.getParentId())
                    .code(m.getCode())
                    .label(m.getLabel())
                    .icon(m.getIcon())
                    .path(m.getPath())
                    .sortOrder(m.getSortOrder())
                    .permissionCode(m.getPermissionCode())
                    .active(m.getActive())
                    .children(new ArrayList<>())
                    .build());
        }

        List<MenuResponse> roots = new ArrayList<>();
        for (MenuResponse node : responseMap.values()) {
            if (node.getParentId() == null || !responseMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                responseMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    private List<MenuResponse> filterByPermissions(List<MenuResponse> nodes, Set<String> permissions) {
        List<MenuResponse> result = new ArrayList<>();
        for (MenuResponse node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                List<MenuResponse> filteredChildren = filterByPermissions(node.getChildren(), permissions);
                // "Menu cha không cần permission riêng, hiện khi có ít nhất một menu con hiện"
                if (!filteredChildren.isEmpty()) {
                    node.setChildren(filteredChildren);
                    result.add(node);
                }
            } else {
                // Leaf node
                if (node.getPermissionCode() == null || node.getPermissionCode().isBlank()
                        || permissions.contains(node.getPermissionCode())) {
                    result.add(node);
                }
            }
        }
        return result;
    }
}
