package com.cnpmnc.document_management.config;

import com.cnpmnc.document_management.entity.Role;
import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.repository.RoleRepository;
import com.cnpmnc.document_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@Order(2) // Run after RoleDataInitializer (which has default order = 1)
@RequiredArgsConstructor
public class UserAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Default admin credentials
    private static final String ADMIN_EMAIL = "user";
    private static final String ADMIN_PASSWORD = "user";
    private static final String ADMIN_FULL_NAME = "System Administrator";

    @Override
    public void run(String... args) {
        log.info("Starting admin account initialization...");

        initializeAdminAccount();

        log.info("Admin account initialization completed.");
    }

    /**
     * Initialize the default admin account if it doesn't exist.
     */
    private void initializeAdminAccount() {

        // Check if admin account already exists
        if (userRepository.existsByUsername(ADMIN_EMAIL)){
            log.debug("Admin account '{}' already exists, skipping initialization.", ADMIN_EMAIL);
            return;
        }

        // Get ADMIN role
        Role adminRole = roleRepository.findByRoleName("USER")
            .orElseThrow(() -> new IllegalStateException(
                "ADMIN role not found. Ensure RoleDataInitializer runs before AdminAccountInitializer."
            ));

        Set<Role> roles = Set.of(adminRole);

        User user = User.builder()
                .username(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .roles(roles)
                .build();

        // Save to database
        userRepository.save(user);
    }
}
