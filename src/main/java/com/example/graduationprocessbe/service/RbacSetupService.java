package com.example.graduationprocessbe.service;
import com.example.graduationprocessbe.dto.request.UpdateMenuRequest;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.repository.*;
import com.example.graduationprocessbe.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
@RequiredArgsConstructor
public class RbacSetupService {
    private final RoleService roles;
    private final MenuService menus;
    private final UserService users;
    private final PermissionService permissions;
    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final UserRoleRepository memberships;
    private final RoleMembershipService membershipService;
    private final ThesisRoundRepository rounds;
    public record Membership(String id,String userId,String roleId,String thesisRoundId) {}
    public record Round(String id,String name,Boolean active) {}
    @Transactional(readOnly = true)
    public Map<String,Object> snapshot(Authentication auth) {
        List<String> own = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        boolean canSeeUsers = own.contains("VIEW_USERS");
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("users",canSeeUsers ? users.getAllUsers() : List.<UserResponse>of());
        result.put("roles",roles.getAllRoles());
        result.put("memberships",canSeeUsers ? memberships.findAll().stream()
            .map(m -> new Membership(m.getId(),m.getUserId(),m.getRoleId(),m.getThesisRoundId())).toList() : List.of());
        result.put("permissions",permissions.getAllPermissions());
        result.put("menus",menuMapper.toResponseList(menuRepository.findAll()));
        result.put("rounds",canSeeUsers ? rounds.findAll().stream().map(r -> new Round(r.getId(),r.getName(),r.getActive())).toList() : List.of());
        result.put("ownPermissions",own);
        return result;
    }
    public void addMember(String userId,String roleId,String roundId) { membershipService.add(userId,roleId,roundId); }
    public void removeMember(String id) { membershipService.remove(id); }
    public void removeMembers(String userId,String roleId) { membershipService.removeAll(userId,roleId); }
    public void configureMenu(String id,UpdateMenuRequest request) { menus.updateMenu(id,request); }
}
