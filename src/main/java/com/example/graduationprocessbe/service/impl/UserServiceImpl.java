package com.example.graduationprocessbe.service.impl;
import com.example.graduationprocessbe.dto.request.*;
import com.example.graduationprocessbe.dto.response.*;
import com.example.graduationprocessbe.entity.*;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.mapper.UserMapper;
import com.example.graduationprocessbe.repository.*;
import com.example.graduationprocessbe.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final UserRoleRepository memberships;
    private final RoleRepository roles;
    private final DepartmentRepository departments;
    private final PasswordEncoder passwords;
    private final UserMapper mapper;
    private final RoleMembershipService membershipService;
    private final RbacAuditService audit;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        // Load relationships once; avoid queries for each account in the setup snapshot.
        var roleMap = roles.findAll().stream().collect(Collectors.toMap(Role::getId,Role::getRoleCode));
        var byUser = memberships.findAll().stream().collect(Collectors.groupingBy(UserRole::getUserId));
        return users.findAll().stream().map(user -> {
            UserResponse response = mapper.toResponse(user);
            response.setUsername(user.getUsername());
            List<String> assigned = byUser.getOrDefault(user.getId(),List.of()).stream()
                .map(m -> roleMap.get(m.getRoleId())).filter(Objects::nonNull).distinct().sorted().toList();
            response.setRoles(assigned); response.setRole(EffectivePermissionService.primaryRole(assigned));
            if (user.getDepartment()!=null) response.setDepartmentName(user.getDepartment().getDeptName());
            return response;
        }).toList();
    }
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (users.existsByEmail(request.getEmail()) || users.existsByUsername(request.getUsername().trim()))
            throw bad("Tên đăng nhập hoặc email đã tồn tại");
        User user = mapper.toEntity(request);
        if (request.getPhone()!=null) user.setPhone(request.getPhone().isBlank()?null:request.getPhone().trim());
        user.setStatus("ACTIVE");
        user.setUserType(request.getUserType()==null ? "STUDENT" : request.getUserType().trim().toUpperCase(Locale.ROOT));
        validateType(user.getUserType());
        if (request.getDepartmentId()!=null && !request.getDepartmentId().isBlank())
            user.setDepartment(departments.findById(request.getDepartmentId()).orElseThrow(() -> bad("Khoa không tồn tại")));
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwords.encode(request.getPassword()));
        users.save(user);
        membershipService.add(user.getId(),user.getUserType(),null);
        if (request.getRoleId()!=null && !request.getRoleId().isBlank()) membershipService.add(user.getId(),request.getRoleId(),null);
        audit.record("USER_CREATED",user.getId(),Map.of(),Map.of("username",user.getUsername(),"type",user.getUserType()));
        return response(user,user.getUsername());
    }
    @Override
    @Transactional
    public UserResponse updateUser(String id,UpdateUserRequest request) {
        // All changes that can remove an active admin serialize on the ADMIN row.
        Role admin = roles.findByRoleCode("ADMIN").orElseThrow(); roles.lockById(admin.getId()).orElseThrow();
        User user = users.findById(id).orElseThrow(() -> bad("Tài khoản không tồn tại"));
        if (!request.getEmail().equals(user.getEmail()) && users.existsByEmail(request.getEmail())) throw bad("Email đã tồn tại");
        boolean isAdmin = memberships.findByUserId(id).stream().anyMatch(m -> admin.getId().equals(m.getRoleId()) && m.getThesisRoundId()==null);
        if (isAdmin && "ACTIVE".equals(user.getStatus()) && !"ACTIVE".equals(request.getStatus()) && memberships.countActiveGlobalAdmins()<=1)
            throw bad("Không được khóa ADMIN hoạt động cuối cùng");
        validateType(request.getUserType());
        if (!user.getUserType().equals(request.getUserType())) throw bad("Không đổi loại hồ sơ của tài khoản đã tạo");
        boolean lecturerRole = memberships.findByUserId(id).stream().anyMatch(m -> roles.findById(m.getRoleId())
            .map(r -> Set.of("LECTURER","FACULTY_STAFF","COMMITTEE").contains(r.getRoleCode())).orElse(false));
        if (lecturerRole && !"LECTURER".equals(request.getUserType())) throw bad("Bỏ vai trò giảng viên/Khoa/Hội đồng trước khi đổi loại tài khoản");
        Map<String,Object> before=new LinkedHashMap<>();
        before.put("name",user.getFullName()); before.put("email",user.getEmail()); before.put("phone",user.getPhone());
        before.put("status",user.getStatus()); before.put("type",user.getUserType());
        user.setFullName(request.getFullName()); user.setEmail(request.getEmail());
        if (request.getPhone()!=null) user.setPhone(request.getPhone().isBlank()?null:request.getPhone().trim());
        user.setStatus(request.getStatus()); user.setUserType(request.getUserType());
        users.save(user);
        Map<String,Object> after=new LinkedHashMap<>();
        after.put("name",user.getFullName()); after.put("email",user.getEmail()); after.put("phone",user.getPhone());
        after.put("status",user.getStatus()); after.put("type",user.getUserType());
        audit.record("USER_UPDATED",id,before,after);
        return response(user,user.getUsername());
    }
    @Override public void assignRoleToUser(String userId,String roleId,String roundId) { membershipService.add(userId,roleId,roundId); }
    @Override public void removeRoleFromUser(String userId,String roleId) { membershipService.removeAll(userId,roleId); }
    private UserResponse response(User user,String username) {
        UserResponse response=mapper.toResponse(user); response.setUsername(username);
        List<String> assigned=memberships.findRoleCodesByUserIdAndRoundId(user.getId(),null);
        response.setRoles(assigned); response.setRole(EffectivePermissionService.primaryRole(assigned)); return response;
    }
    private void validateType(String type) { if (!Set.of("ADMIN","LECTURER","STUDENT").contains(type)) throw bad("Loại tài khoản không hợp lệ"); }
    private ApplicationException bad(String text) { return new ApplicationException("INVALID_USER",text,HttpStatus.BAD_REQUEST); }
}
