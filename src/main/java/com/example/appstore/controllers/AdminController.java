package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.services.UserService;
import com.example.appstore.services.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService    userService;
    private final WarningService warningService;

    @Autowired
    public AdminController(UserService userService,
                           WarningService warningService) {
        this.userService    = userService;
        this.warningService = warningService;
    }

    @GetMapping({ "", "/dashboard" })
    public String adminDashboard(Model model) {
        model.addAttribute("user", new User());
        return "admin";
    }

    @PostMapping("/addAdmin")
    public String addAdmin(@ModelAttribute("user") User user,
                           RedirectAttributes attrs) {
        if (userService.emailExists(user.getEmail())) {
            attrs.addFlashAttribute("error", "Email already in use");
        } else {
            user.setRole("ADMIN");
            userService.registerUser(user);
            attrs.addFlashAttribute("message", "Admin added successfully");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users/data")
    @ResponseBody
    public List<Map<String, Object>> getUsersData() {
        return userService.getAllUsers().stream()
                .map(u -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("id",        u.getId());
                    m.put("email",     u.getEmail());
                    m.put("role",      u.getRole());
                    m.put("suspended", u.isSuspended());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/users/manageSuspensions")
    public String manageSuspensions(Model model) {
        List<User> suspended = userService.getAllUsers().stream()
                .filter(User::isSuspended)
                .collect(Collectors.toList());
        model.addAttribute("users", suspended);
        return "manageSuspensions";
    }

    @PostMapping("/user/{id}/unsuspend")
    public String unsuspendUser(@PathVariable Long id,
                                RedirectAttributes attrs) {
        User u = userService.getUserById(id);
        if (u != null && u.isSuspended()) {
            warningService.clearWarnings(u);
            u.setSuspended(false);
            userService.save(u);
            attrs.addFlashAttribute("message",
                    "User " + u.getEmail() + " has been reactivated and warnings cleared.");
        } else {
            attrs.addFlashAttribute("error",
                    "User not found or not currently suspended.");
        }
        return "redirect:/admin/users/manageSuspensions";
    }
}
