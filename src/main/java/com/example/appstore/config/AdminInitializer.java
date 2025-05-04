package com.example.appstore.config;

import com.example.appstore.models.User;
import com.example.appstore.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserService userService;

    public AdminInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = System.getenv("ADMIN_USERNAME");
        String pass  = System.getenv("ADMIN_PASSWORD");
        if (email == null || pass == null) {
            System.out.println("Skipping admin init; ADMIN_USERNAME/PASSWORD not set");
            return;
        }

        if (!userService.emailExists(email)) {
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(pass);
            admin.setRole("ADMIN");
            userService.registerUser(admin);
            System.out.println("Bootstrapped ADMIN: " + email);
        }
    }
}
