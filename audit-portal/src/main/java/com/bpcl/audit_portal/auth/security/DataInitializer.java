package com.bpcl.audit_portal.auth.security;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.model.Role;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.repository.RoleRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder) {

        return args -> {
            if (!userRepository.existsByUserName("admin@bpcl.com")) {

                Role adminRole = roleRepository
                        .findByRoleName(AppRole.ADMIN)
                        .orElseThrow();

                User admin = User.builder()
                        .userName("admin@bpcl.com")
                        .fullName("System Administrator")
                        .password(encoder.encode("Admin@123"))
                        .role(adminRole)
                        .isActive(true)
                        .logout(true)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}