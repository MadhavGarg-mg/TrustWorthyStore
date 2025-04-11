package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.models.AppEntry;
import com.example.appstore.repository.UserRepository;
import com.example.appstore.repository.AppEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppEntryRepository appEntryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<User> users = userRepository.findAll();
        List<AppEntry> apps = appEntryRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("apps", apps);
        return "admin";
    }

    @PostMapping("/addAdmin")
    public String addAdmin(@RequestParam String email, @RequestParam String password, Model model) {
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "User already exists!");
        } else {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("ADMIN");
            userRepository.save(user);
            model.addAttribute("message", "Admin added successfully!");
        }
        return "redirect:/admin/dashboard";
    }
}
