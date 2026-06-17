package org.example.userservice.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.model.enums.Role;
import org.example.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@library.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("[DataInitializer] Admin user created.");
        } else {
            log.info("[DataInitializer] Admin user already exists, skipping.");
        }

        if (!userRepository.existsByUsername("librarian")) {
            User librarian = new User();
            librarian.setUsername("librarian");
            librarian.setEmail("librarian@library.com");
            librarian.setPassword(passwordEncoder.encode("librarian123"));
            librarian.setRole(Role.LIBRARIAN);
            librarian.setActive(true);
            userRepository.save(librarian);
            log.info("[DataInitializer] Librarian user created.");
        } else {
            log.info("[DataInitializer] Librarian user already exists, skipping.");
        }
    }
}