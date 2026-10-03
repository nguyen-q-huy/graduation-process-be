package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CreateMenuRequest;
import com.example.graduationprocessbe.dto.request.UpdateMenuRequest;
import com.example.graduationprocessbe.dto.response.MenuResponse;
import com.example.graduationprocessbe.entity.Security;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.repository.SecurityRepository;
import com.example.graduationprocessbe.security.CustomUserDetails;
import com.example.graduationprocessbe.service.MenuService;
import com.example.graduationprocessbe.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final SecurityRepository securityRepository;

    @GetMapping("/my-menus")
    public ResponseEntity<ApiResponseWrapper<List<MenuResponse>>> getMyMenus(
            Authentication authentication,
            @RequestParam(required = false) String roundId) {

        String userId;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUserId();
        } else {
            Security security = securityRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            userId = security.getUserId();
        }

        List<MenuResponse> menus = menuService.getUserMenus(userId, roundId);

        return ResponseEntity.ok(new ApiResponseWrapper<>(
                ResponseDetails.API_SUCCESSFULLY,
                menus
        ));
    }

    @GetMapping("/master")
    public ResponseEntity<ApiResponseWrapper<List<MenuResponse>>> getMasterMenus() {
        List<MenuResponse> menus = menuService.getMasterMenuTree();
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                ResponseDetails.API_SUCCESSFULLY,
                menus
        ));
    }

    @GetMapping("/master-page")
    public ResponseEntity<ApiResponseWrapper<?>> getMasterMenusPage(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {
        String keyword = PaginationUtils.normalize(search);
        List<MenuResponse> flatMenus = flattenMenus(menuService.getMasterMenuTree());
        List<MenuResponse> filtered = flatMenus.stream()
                .filter(menu -> keyword.isBlank()
                        || PaginationUtils.normalize(menu.getLabel()).contains(keyword)
                        || PaginationUtils.normalize(menu.getCode()).contains(keyword)
                        || PaginationUtils.normalize(menu.getPath()).contains(keyword)
                        || PaginationUtils.normalize(menu.getPermissionCode()).contains(keyword))
                .toList();
        Object data = page == null && size == null ? filtered : PaginationUtils.page(filtered, page, size);
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                ResponseDetails.API_SUCCESSFULLY,
                data
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<MenuResponse>> createMenu(
            @Valid @RequestBody CreateMenuRequest request) {
        return ResponseEntity.status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        menuService.createMenu(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<MenuResponse>> updateMenu(
            @PathVariable String id,
            @Valid @RequestBody UpdateMenuRequest request) {
        return ResponseEntity.status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        menuService.updateMenu(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteMenu(
            @PathVariable String id) {
        menuService.deleteMenu(id);
        return ResponseEntity.status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        null));
    }

    private List<MenuResponse> flattenMenus(List<MenuResponse> menus) {
        List<MenuResponse> result = new ArrayList<>();
        for (MenuResponse menu : menus) {
            result.add(menu);
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                result.addAll(flattenMenus(menu.getChildren()));
            }
        }
        return result;
    }
}
