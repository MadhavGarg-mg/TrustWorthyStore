package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RegisterController {

    private static final List<String> ALLOWED_ROLES = List.of("USER", "DEVELOPER");

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        model.addAttribute("roles", ALLOWED_ROLES);
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(
            @ModelAttribute("user") User user,
            Model model,
            Authentication authentication) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already in use");
            model.addAttribute("roles", ALLOWED_ROLES);
            return "register";
        }

        // Enforce valid role
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            user.setRole("USER");
        }

        // Persist (registerUser encodes the password and saves)
        userService.registerUser(user);

        return "redirect:/login?registered";
    }
}
