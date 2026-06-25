package com.hsms.authservice.config;

import com.hsms.authservice.entity.Role;
import com.hsms.authservice.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DbRoleInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DbRoleInitializer.class);
    private final RoleRepository roleRepository;

    public DbRoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> defaultRoles = List.of("CUSTOMER", "ADMIN", "TECHNICIAN", "SERVICE_MANAGER");

        for (String roleName : defaultRoles) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
                log.info("Seeded role: {}", roleName);
            }
        }
    }
}
