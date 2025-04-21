package com.example.appstore.services;

import com.example.appstore.models.User;
import com.example.appstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new UserService(userRepo, passwordEncoder);
    }

    @Test
    void registerUser_encodesPasswordAndSaves() {
        User u = new User();
        u.setPassword("raw");
        when(passwordEncoder.encode("raw")).thenReturn("encoded");

        service.registerUser(u);

        assertEquals("encoded", u.getPassword());
        verify(userRepo).save(u);
    }

    @Test
    void getUserById_found() {
        User u = new User();
        when(userRepo.findById(10L)).thenReturn(Optional.of(u));

        assertSame(u, service.getUserById(10L));
    }

    @Test
    void getUserById_notFound() {
        when(userRepo.findById(11L)).thenReturn(Optional.empty());

        assertNull(service.getUserById(11L));
    }

    @Test
    void getAllUsers_returnsList() {
        List<User> lst = Arrays.asList(new User(), new User());
        when(userRepo.findAll()).thenReturn(lst);

        assertSame(lst, service.getAllUsers());
    }

    @Test
    void emailExists_trueAndFalse() {
        when(userRepo.findByEmail("a@b")).thenReturn(Optional.of(new User()));
        assertTrue(service.emailExists("a@b"));

        when(userRepo.findByEmail("c@d")).thenReturn(Optional.empty());
        assertFalse(service.emailExists("c@d"));
    }

    @Test
    void getUserByEmail_foundAndNull() {
        User u = new User();
        when(userRepo.findByEmail("e@f")).thenReturn(Optional.of(u));
        assertSame(u, service.getUserByEmail("e@f"));

        when(userRepo.findByEmail("g@h")).thenReturn(Optional.empty());
        assertNull(service.getUserByEmail("g@h"));
    }
}
