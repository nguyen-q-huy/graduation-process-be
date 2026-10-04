package com.example.graduationprocessbe.service;
import com.example.graduationprocessbe.entity.Menu;
import com.example.graduationprocessbe.entity.Permission;
import com.example.graduationprocessbe.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.stream.Collectors;
public final class PermissionPolicy {
    private PermissionPolicy() {}
    public static List<String> normalize(Collection<String> requested,List<Permission> permissions,List<Menu> menus) {
        if (requested == null) throw bad("Danh sách quyền không được bỏ trống");
        Set<String> ids = new LinkedHashSet<>(requested);
        Map<String,Permission> byId = permissions.stream().collect(Collectors.toMap(Permission::getId,p -> p));
        if (!byId.keySet().containsAll(ids)) throw bad("Quyền không tồn tại");
        Set<String> codes = ids.stream().map(id -> byId.get(id).getCode()).collect(Collectors.toSet());
        Map<String,Menu> byCode = menus.stream().collect(Collectors.toMap(Menu::getCode,m -> m));
        for (String id : ids) {
            Permission p = byId.get(id);
            if (!Boolean.TRUE.equals(p.getEnabled())) throw bad("Quyền chưa được hỗ trợ hoặc đã bị tắt");
            if (p.getMenuCode() == null || p.getAction() == null || "VIEW".equals(p.getAction())) continue;
            Menu menu = byCode.get(p.getMenuCode());
            if (menu == null || !codes.contains(menu.getPermissionCode())) throw bad("Cần cấp quyền xem trước khi cấp thao tác");
        }
        return new ArrayList<>(ids);
    }
    private static ApplicationException bad(String text) { return new ApplicationException("INVALID_PERMISSION",text,HttpStatus.BAD_REQUEST); }
}
