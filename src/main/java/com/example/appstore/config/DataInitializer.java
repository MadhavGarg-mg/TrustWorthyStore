package com.example.appstore.config;

import com.example.appstore.models.User;
import com.example.appstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@appstore.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@appstore.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Default admin created: admin@appstore.com / Admin@123");
        }
    }
}
