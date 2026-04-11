package com.cnpmnc.document_management.config;

import com.cnpmnc.document_management.entity.Role;
import com.cnpmnc.document_management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@Order(1) // Run first, before AdminAccountInitializer
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Starting role data initialization...");

        initializeRole("USER", "Student role - can borrow books and make reservations");
        initializeRole("ADMIN", "Administrator role - has full access to all system features");

        log.info("Role data initialization completed.");
    }

    /**
     * Initialize a role if it doesn't exist.
     *
     * @param roleName the role name
     * @param description the role description
     */
    private void initializeRole(String roleName, String description) {
        if (roleRepository.existsByRoleName(roleName)) {
            log.debug("Role '{}' already exists, skipping initialization.", roleName);
            return;
        }

        Role role = Role.builder()
                .roleName(roleName)
                .description(description)
                .build();
        roleRepository.save(role);
    }
}
