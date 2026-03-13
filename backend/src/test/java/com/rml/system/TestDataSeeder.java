package com.rml.system;

import com.rml.system.entity.Role;
import com.rml.system.entity.User;
import com.rml.system.repository.RoleRepository;
import com.rml.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds a predictable admin user for integration tests.
 * Uses fixed IDs so test-seed.sql can reference them by ID.
 * Runs once at application context startup (before any @Sql scripts).
 * Password = "admin123" — matches FeatureApiContractIntegrationTest.login_validCredentials.
 */
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("admin")) return;

        Role admin = new Role();
        admin.setId("0");
        admin.setName("ADMIN");
        admin.setDescription("Full access");
        roleRepository.save(admin);

        Role guest = new Role();
        guest.setId("1");
        guest.setName("GUEST");
        guest.setDescription("Read-only");
        roleRepository.save(guest);

        User user = new User();
        user.setId("0");
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setPasswordResetRequired(false);
        user.setRoles(Set.of(admin));
        userRepository.save(user);
    }
}
