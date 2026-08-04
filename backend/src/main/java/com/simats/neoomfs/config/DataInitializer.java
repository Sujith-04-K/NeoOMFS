package com.simats.neoomfs.config;

import com.simats.neoomfs.entity.Role;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.repository.RoleRepository;
import com.simats.neoomfs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Automatically seeds default roles and demo institutional accounts on startup
 * if they do not exist. Ensures immediate testability in dev and test modes.
 */
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
        log.info("Checking database seeding status...");

        // 1. Seed Roles
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }

        // 2. Seed Default Institutional Users
        seedUser("doctor@simats.ac.in", "dr.arvind", "Dr. Arvind Kumar", "Password@123", Role.RoleName.ROLE_DOCTOR, "MDS-OMFS-104");
        seedUser("student@simats.ac.in", "sujith.k", "Sujith K (BDS/MDS Student)", "Password@123", Role.RoleName.ROLE_STUDENT, "STU-2024-098");
        seedUser("faculty@simats.ac.in", "dr.lakshmi", "Dr. Lakshmi Raman (Head Faculty)", "Password@123", Role.RoleName.ROLE_FACULTY, "MDS-OMFS-001");
        seedUser("admin@simats.ac.in", "admin", "System Administrator", "Password@123", Role.RoleName.ROLE_ADMIN, "ADM-001");

        log.info("Database seeding verification complete.");
    }

    private void seedUser(String email, String username, String fullName, String rawPassword, Role.RoleName roleName, String licenseNumber) {
        if (userRepository.findByEmail(email).isEmpty()) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isPresent()) {
                User user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setFullName(fullName);
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setLicenseNumber(licenseNumber);
                user.setRoles(Set.of(roleOpt.get()));
                user.setActive(true);
                userRepository.save(user);
                log.info("Seeded user account: {} [{}]", email, roleName);
            }
        }
    }
}
