package com.bortnik.bank_rest.config;

import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.entity.User;
import com.bortnik.bank_rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            if (userRepository.existsByUsername(adminUsername)) {
                log.info("Admin user already exists");
            } else {
                userRepository.save(
                        User.builder()
                                .username(adminUsername)
                                .password(passwordEncoder.encode(adminPassword))
                                .role(Role.ADMIN)
                                .build()
                );
                log.info("Admin user created with username: {}", adminUsername);
            }
        };
    }
}
