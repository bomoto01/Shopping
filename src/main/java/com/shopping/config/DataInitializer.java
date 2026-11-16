package com.shopping.config;

import com.shopping.entity.Role;
import com.shopping.entity.User;
import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.repository.RoleRepository;
import com.shopping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initDefaultAdmin();
    }

    private void initRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepository.findByRoleCode(roleType).isEmpty()) {
                String roleName = switch (roleType) {
                    case ROLE_ADMIN -> "Quản trị viên";
                    case ROLE_STAFF -> "Nhân viên quản lý";
                    case ROLE_CUSTOMER -> "Khách hàng";
                };
                Role role = Role.builder()
                        .roleCode(roleType)
                        .roleName(roleName)
                        .build();
                roleRepository.save(role);
                log.info("Khởi tạo vai trò mặc định: {}", roleType);
            }
        }
    }

    private void initDefaultAdmin() {
        String adminEmail = "admin@shopping.vn";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByRoleCode(RoleType.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN chưa được khởi tạo"));
            Role staffRole = roleRepository.findByRoleCode(RoleType.ROLE_STAFF)
                    .orElseThrow(() -> new IllegalStateException("ROLE_STAFF chưa được khởi tạo"));

            User admin = User.builder()
                    .fullName("Quản Trị Viên Hệ Thống")
                    .email(adminEmail)
                    .phone("0988888888")
                    .passwordHash(passwordEncoder.encode("Admin@123456"))
                    .allowPersonalization(true)
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(adminRole, staffRole)))
                    .build();

            userRepository.save(admin);
            log.info("Khởi tạo tài khoản quản trị mặc định: {} / Admin@123456", adminEmail);
        }
    }
}
