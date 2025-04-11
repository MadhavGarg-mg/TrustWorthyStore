package com.example.appstore.services;

import com.example.appstore.models.User;
import com.example.appstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String email, String password, String role) {
        logger.info("🔍 Checking if email already exists: " + email);
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warning("❌ Email already exists: " + email);
            throw new RuntimeException("Email already exists!");
        }
        logger.info("🔐 Hashing password...");
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, role);
        logger.info("📌 Saving user to database: " + email);
        userRepository.save(user);
        logger.info("✅ User successfully saved: " + email);
        return user;
    }

    public Optional<User> loginUser(String email, String password) {
        logger.info("🔍 Trying to log in user: " + email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warning("❌ User not found: " + email);
            return Optional.empty();
        }
        User user = userOpt.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            logger.info("✅ Login successful for: " + email);
            return Optional.of(user);
        } else {
            logger.warning("❌ Password mismatch for: " + email);
            return Optional.empty();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
