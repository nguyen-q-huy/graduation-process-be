package com.example.graduationprocessbe.config;

import com.example.graduationprocessbe.entity.*;
import com.example.graduationprocessbe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @org.springframework.beans.factory.annotation.Value("${app.bootstrap.password:}")
    private String bootstrapPassword;
    @org.springframework.beans.factory.annotation.Value("${app.seed-demo:false}")
    private boolean seedDemo;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // The versioned SQL is also used for upgrades of an existing database.
        // Execute as one PostgreSQL script (DO block), not a semicolon-split script.
        String seed = new ClassPathResource("seed_rbac_and_menus.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        jdbcTemplate.execute(seed);
        if (!jdbcTemplate.queryForObject("select exists(select 1 from app_seed_versions where version='role-rbac-v6')",Boolean.class)) {
            jdbcTemplate.execute(new ClassPathResource("seed_permission_layers.sql").getContentAsString(StandardCharsets.UTF_8));
        }
        jdbcTemplate.execute(new ClassPathResource("migrate_role_rbac.sql").getContentAsString(StandardCharsets.UTF_8));
        jdbcTemplate.execute(new ClassPathResource("remove_permission_catalogue.sql").getContentAsString(StandardCharsets.UTF_8));
        jdbcTemplate.execute(new ClassPathResource("compact_rbac_catalogue.sql").getContentAsString(StandardCharsets.UTF_8));

        jdbcTemplate.execute(new ClassPathResource("migrate_actor_rbac.sql").getContentAsString(StandardCharsets.UTF_8));
        jdbcTemplate.execute(new ClassPathResource("migrate_actor_constraints.sql").getContentAsString(StandardCharsets.UTF_8));

        ensureUser("admin", "admin@graduation.local", "Quản trị viên Nguyễn Văn An", role("ADMIN"), "ADMIN");
        if (seedDemo) {
            ensureUser("khoa", "khoa@graduation.local", "Giảng viên phụ trách Khoa CNTT", role("FACULTY_STAFF"), "LECTURER");
            ensureUser("lecturer", "lecturer@graduation.local", "TS. Nguyễn Văn Tuấn", role("LECTURER"), "LECTURER");
            ensureUser("student", "student@graduation.local", "Trần Minh Quân", role("STUDENT"), "STUDENT");
        }
        if (userRoleRepository.countActiveGlobalAdmins()==0)
            throw new IllegalStateException("Cần ít nhất một ADMIN toàn hệ thống đang hoạt động");
        log.info("Versioned RBAC/menu initialization completed.");
    }

    private Role role(String code) {
        return roleRepository.findByRoleCode(code).orElseThrow();
    }

    private void ensureUser(String username, String email, String fullName, Role role, String userType) {
        // Never reset an existing account's password or role assignments on startup.
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        if (bootstrapPassword == null || bootstrapPassword.length()<6)
            throw new IllegalStateException("Cần cấu hình APP_BOOTSTRAP_PASSWORD ít nhất 6 ký tự để tạo tài khoản mới");
        String encodedPassword = passwordEncoder.encode(bootstrapPassword);
        if (userRepository.existsByEmail(email)) throw new IllegalStateException("Email bootstrap đã thuộc tài khoản khác");
        User user = new User(); user.setEmail(email); user.setFullName(fullName);
        user.setUsername(username); user.setPasswordHash(encodedPassword);
        user.setUserType(userType); user.setStatus("ACTIVE");
        User savedUser = userRepository.save(user);

        ensureUserRole(savedUser.getId(), role);
        if (Set.of("FACULTY_STAFF", "COMMITTEE").contains(role.getRoleCode())) {
            ensureUserRole(savedUser.getId(), role("LECTURER"));
        }
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
