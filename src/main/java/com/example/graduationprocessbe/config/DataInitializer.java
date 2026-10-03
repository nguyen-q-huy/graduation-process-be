package com.example.graduationprocessbe.config;

import com.example.graduationprocessbe.entity.*;
import com.example.graduationprocessbe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "11111";

    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Initializing RBAC roles, permissions, menus and users...");

        // 1. Roles
        Role adminRole = ensureRole("ADMIN", "Quản trị viên hệ thống");
        Role facultyRole = ensureRole("FACULTY_STAFF", "Cán bộ quản lý Khoa");
        Role lecturerRole = ensureRole("LECTURER", "Giảng viên");
        Role committeeRole = ensureRole("COMMITTEE", "Thành viên Hội đồng bảo vệ");
        Role studentRole = ensureRole("STUDENT", "Sinh viên làm ĐATN");

        // 2. Permissions
        Map<String, Permission> perms = new HashMap<>();
        perms.put("DASHBOARD_VIEW", ensurePermission("DASHBOARD_VIEW", "Xem Dashboard tổng quan", "DASHBOARD"));
        perms.put("THESIS_ROUND_MANAGE", ensurePermission("THESIS_ROUND_MANAGE", "Quản lý Đợt đồ án", "ROUND"));
        perms.put("TOPIC_VIEW", ensurePermission("TOPIC_VIEW", "Xem danh sách Đề tài", "TOPIC"));
        perms.put("TOPIC_CREATE", ensurePermission("TOPIC_CREATE", "Đề xuất Đề tài mới", "TOPIC"));
        perms.put("TOPIC_APPROVE", ensurePermission("TOPIC_APPROVE", "Phê duyệt Đề tài", "TOPIC"));
        perms.put("TOPIC_SUGGEST", ensurePermission("TOPIC_SUGGEST", "Ngân hàng đề tài gợi ý", "TOPIC"));
        perms.put("ADVISOR_ASSIGN", ensurePermission("ADVISOR_ASSIGN", "Phân công Giảng viên hướng dẫn", "ASSIGNMENT"));
        perms.put("OUTLINE_APPROVE", ensurePermission("OUTLINE_APPROVE", "Phê duyệt Đề cương chi tiết", "PROGRESS"));
        perms.put("OVERDUE_TRACK", ensurePermission("OVERDUE_TRACK", "Theo dõi trễ hạn tự động", "PROGRESS"));
        perms.put("SIMILARITY_CHECK", ensurePermission("SIMILARITY_CHECK", "Kiểm tra trùng lắp & đạo văn", "PROGRESS"));
        perms.put("EXCEPTION_HANDLE", ensurePermission("EXCEPTION_HANDLE", "Can thiệp ngoại lệ & Đơn từ", "PROGRESS"));
        perms.put("COMMITTEE_MANAGE", ensurePermission("COMMITTEE_MANAGE", "Quản lý & Xếp lịch Hội đồng", "COMMITTEE"));
        perms.put("COMMITTEE_SCORE", ensurePermission("COMMITTEE_SCORE", "Nhập điểm chấm bảo vệ", "COMMITTEE"));
        perms.put("WORKFLOW_CONFIG", ensurePermission("WORKFLOW_CONFIG", "Cấu hình quy trình & Ngưỡng", "CONFIG"));
        perms.put("TEMPLATE_MANAGE", ensurePermission("TEMPLATE_MANAGE", "Quản lý Biểu mẫu & Email nhắc hạn", "TEMPLATE"));
        perms.put("USER_MANAGE", ensurePermission("USER_MANAGE", "Quản lý Người dùng & Phân quyền", "SYSTEM"));
        perms.put("REPORT_VIEW", ensurePermission("REPORT_VIEW", "Báo cáo & Thống kê", "REPORT"));
        perms.put("SYSTEM_CONFIG", ensurePermission("SYSTEM_CONFIG", "Cài đặt hệ thống & Audit logs", "SYSTEM"));

        // Student-specific
        perms.put("STUDENT_PROPOSAL", ensurePermission("STUDENT_PROPOSAL", "Đăng ký đề tài & Chọn GVHD", "STUDENT"));
        perms.put("STUDENT_PROGRESS_REPORT", ensurePermission("STUDENT_PROGRESS_REPORT", "Nộp báo cáo tuần/tháng", "STUDENT"));
        perms.put("STUDENT_FINAL_SUBMIT", ensurePermission("STUDENT_FINAL_SUBMIT", "Nộp báo cáo chính thức & Source", "STUDENT"));

        // 3. Role Permissions Mapping
        // ADMIN gets all permissions
        for (Permission p : perms.values()) {
            ensureRolePermission(adminRole.getId(), p.getId());
        }

        // FACULTY_STAFF permissions
        List<String> facultyPermCodes = List.of(
                "DASHBOARD_VIEW", "THESIS_ROUND_MANAGE", "TOPIC_VIEW", "TOPIC_APPROVE", "TOPIC_SUGGEST",
                "ADVISOR_ASSIGN", "OUTLINE_APPROVE", "OVERDUE_TRACK", "SIMILARITY_CHECK", "EXCEPTION_HANDLE",
                "COMMITTEE_MANAGE", "WORKFLOW_CONFIG", "TEMPLATE_MANAGE", "REPORT_VIEW"
        );
        for (String code : facultyPermCodes) {
            ensureRolePermission(facultyRole.getId(), perms.get(code).getId());
        }

        // LECTURER permissions
        List<String> lecturerPermCodes = List.of(
                "DASHBOARD_VIEW", "TOPIC_VIEW", "TOPIC_CREATE", "OUTLINE_APPROVE", "COMMITTEE_SCORE"
        );
        for (String code : lecturerPermCodes) {
            ensureRolePermission(lecturerRole.getId(), perms.get(code).getId());
        }

        // COMMITTEE permissions
        List<String> committeePermCodes = List.of(
                "DASHBOARD_VIEW", "COMMITTEE_SCORE"
        );
        for (String code : committeePermCodes) {
            ensureRolePermission(committeeRole.getId(), perms.get(code).getId());
        }

        // STUDENT permissions
        List<String> studentPermCodes = List.of(
                "DASHBOARD_VIEW", "STUDENT_PROPOSAL", "STUDENT_PROGRESS_REPORT", "STUDENT_FINAL_SUBMIT"
        );
        for (String code : studentPermCodes) {
            ensureRolePermission(studentRole.getId(), perms.get(code).getId());
        }

        // 4. Menus (Hierarchical with permission_code)
        initMenus();

        // 5. Users
        ensureUser("admin", "admin@graduation.local", "Quản trị viên Nguyễn Văn An", adminRole, "ADMIN");
        ensureUser("khoa", "khoa@graduation.local", "Cán bộ Khoa CNTT", facultyRole, "STAFF");
        ensureUser("lecturer", "lecturer@graduation.local", "TS. Nguyễn Văn Tuấn", lecturerRole, "LECTURER");
        ensureUser("student", "student@graduation.local", "Trần Minh Quân", studentRole, "STUDENT");

        log.info("RBAC initialization completed successfully!");
    }

    private Role ensureRole(String roleCode, String roleName) {
        return roleRepository.findByRoleCode(roleCode)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleCode(roleCode);
                    role.setRoleName(roleName);
                    return roleRepository.save(role);
                });
    }

    private Permission ensurePermission(String code, String name, String module) {
        return permissionRepository.findByCode(code)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode(code);
                    p.setName(name);
                    p.setModule(module);
                    return permissionRepository.save(p);
                });
    }

    private void ensureRolePermission(String roleId, String permissionId) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionRepository.save(rp);
        }
    }

    private void initMenus() {
        // TỔNG QUAN
        ensureMenu(null, "tong-quan", "Tổng quan (Dashboard)", "DashboardOutlined", "/dashboard", 1, "DASHBOARD_VIEW");

        // VẬN HÀNH
        ensureMenu(null, "dot-do-an", "Đợt đồ án", "CalendarOutlined", "/rounds", 10, "THESIS_ROUND_MANAGE");

        Menu deTaiParent = ensureMenu(null, "quan-ly-de-tai", "Đề tài", "BookOutlined", "/topics", 20, null);
        ensureMenu(deTaiParent.getId(), "ds-de-tai", "Tất cả đề tài", null, "/topics/all", 21, "TOPIC_VIEW");
        ensureMenu(deTaiParent.getId(), "duyet-de-tai", "Phê duyệt đề tài mới", null, "/topics/approve", 22, "TOPIC_APPROVE");
        ensureMenu(deTaiParent.getId(), "ngan-hang-de-tai", "Ngân hàng đề tài gợi ý", null, "/topics/suggested", 23, "TOPIC_SUGGEST");

        ensureMenu(null, "phan-cong", "Phân công hướng dẫn", "UserSwitchOutlined", "/advisor-assign", 30, "ADVISOR_ASSIGN");

        Menu hoSoParent = ensureMenu(null, "ho-so-tien-do", "Hồ sơ & Tiến độ", "ClockCircleOutlined", "/progress", 40, null);
        ensureMenu(hoSoParent.getId(), "duyet-de-cuong", "Duyệt đề cương", "CheckSquareOutlined", "/progress/outline", 41, "OUTLINE_APPROVE");
        ensureMenu(hoSoParent.getId(), "theo-doi-tre-han", "Theo dõi trễ hạn", "ClockCircleOutlined", "/progress/overdue", 42, "OVERDUE_TRACK");
        ensureMenu(hoSoParent.getId(), "trung-lap", "Trùng lắp & Đạo văn", "SafetyCertificateOutlined", "/progress/similarity", 43, "SIMILARITY_CHECK");
        ensureMenu(hoSoParent.getId(), "can-thiep-ngoai-le", "Can thiệp ngoại lệ", "ExclamationCircleOutlined", "/progress/exceptions", 44, "EXCEPTION_HANDLE");

        Menu hoiDongParent = ensureMenu(null, "hoi-dong", "Hội đồng bảo vệ", "ScheduleOutlined", "/committee", 50, null);
        ensureMenu(hoiDongParent.getId(), "ds-hoi-dong", "Danh sách Hội đồng", null, "/committee/list", 51, "COMMITTEE_MANAGE");
        ensureMenu(hoiDongParent.getId(), "xep-lich-phong", "Xếp lịch & Phòng bảo vệ", null, "/committee/schedule", 52, "COMMITTEE_MANAGE");
        ensureMenu(hoiDongParent.getId(), "cham-diem-hoi-dong", "Nhập điểm bảo vệ", null, "/committee/scoring", 53, "COMMITTEE_SCORE");

        // CẤU HÌNH & TỰ ĐỘNG HÓA
        ensureMenu(null, "cau-hinh-quy-trinh", "Cấu hình quy trình", "ControlOutlined", "/workflow-config", 60, "WORKFLOW_CONFIG");
        ensureMenu(null, "bieu-mau-thong-bao", "Biểu mẫu & Thông báo", "MailOutlined", "/templates", 70, "TEMPLATE_MANAGE");

        // HỆ THỐNG
        Menu userPermissionParent = ensureMenu(null, "nguoi-dung-phan-quyen", "Người dùng & Phân quyền", "TeamOutlined", "/users", 80, "USER_MANAGE");
        ensureMenu(userPermissionParent.getId(), "users", "Tài khoản người dùng", "UserOutlined", "/admin/users", 81, "USER_MANAGE");
        ensureMenu(userPermissionParent.getId(), "roles", "Nhóm vai trò", "SafetyCertificateOutlined", "/admin/roles", 82, "USER_MANAGE");
        ensureMenu(userPermissionParent.getId(), "permissions", "Danh mục quyền", "KeyOutlined", "/admin/permissions", 83, "USER_MANAGE");
        ensureMenu(userPermissionParent.getId(), "menus", "Menu & URL", "MenuOutlined", "/admin/menus", 84, "USER_MANAGE");
        ensureMenu(null, "bao-cao-thong-ke", "Báo cáo & Thống kê", "BarChartOutlined", "/reports", 90, "REPORT_VIEW");
        ensureMenu(null, "cai-dat-he-thong", "Cài đặt hệ thống & Audit", "SettingOutlined", "/settings", 100, "SYSTEM_CONFIG");

        // ĐỒ ÁN CỦA TÔI
        ensureMenu(null, "dang-ky-de-tai", "Đăng ký đề tài & Chọn GVHD", "BookOutlined", "/student/proposal", 110, "STUDENT_PROPOSAL");
        ensureMenu(null, "nhat-ky-tien-do", "Báo cáo tiến độ tuần", "ClockCircleOutlined", "/student/progress", 120, "STUDENT_PROGRESS_REPORT");
        ensureMenu(null, "nop-bao-cao", "Nộp báo cáo chính thức", "UploadOutlined", "/student/submit", 130, "STUDENT_FINAL_SUBMIT");
    }

    private Menu ensureMenu(String parentId, String code, String label, String icon, String path, int sortOrder, String permissionCode) {
        return menuRepository.findByCode(code)
                .map(existing -> {
                    existing.setParentId(parentId);
                    existing.setLabel(label);
                    existing.setIcon(icon);
                    existing.setPath(path);
                    existing.setSortOrder(sortOrder);
                    existing.setPermissionCode(permissionCode);
                    existing.setActive(true);
                    return menuRepository.save(existing);
                })
                .orElseGet(() -> {
                    Menu m = new Menu();
                    m.setParentId(parentId);
                    m.setCode(code);
                    m.setLabel(label);
                    m.setIcon(icon);
                    m.setPath(path);
                    m.setSortOrder(sortOrder);
                    m.setPermissionCode(permissionCode);
                    m.setActive(true);
                    return menuRepository.save(m);
                });
    }

    private void ensureUser(String username, String email, String fullName, Role role, String userType) {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        Security existingSecurity = securityRepository.findByUsername(username).orElse(null);
        if (existingSecurity != null) {
            existingSecurity.setPasswordHash(encodedPassword);
            securityRepository.save(existingSecurity);
            userRepository.findById(existingSecurity.getUserId()).ifPresent(user -> {
                user.setUsername(username);
                user.setPassword(encodedPassword);
                user.setUserType(userType);
                userRepository.save(user);
            });
            ensureUserRole(existingSecurity.getUserId(), role);
            return;
        }

        User savedUser = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setFullName(fullName);
                    user.setUsername(username);
                    user.setPassword(encodedPassword);
                    user.setUserType(userType);
                    user.setStatus("ACTIVE");
                    return userRepository.save(user);
                });

        Security security = new Security();
        security.setUserId(savedUser.getId());
        security.setUsername(username);
        security.setPasswordHash(encodedPassword);
        securityRepository.save(security);

        ensureUserRole(savedUser.getId(), role);
    }

    private void ensureUserRole(String userId, Role role) {
        if (!userRoleRepository.existsByUserIdAndRoleId(userId, role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setThesisRoundId(null); // Global role
            userRoleRepository.save(userRole);
        }
    }
}
