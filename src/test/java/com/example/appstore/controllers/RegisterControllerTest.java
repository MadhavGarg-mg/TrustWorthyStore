package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegisterControllerTest {

    @InjectMocks
    private RegisterController controller;

    @Mock
    private UserService userService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("");
        vr.setSuffix(".html");
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(vr)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void showForm() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void submitRegistersAndRedirects() throws Exception {
        mvc.perform(post("/register")
                        .param("email", "a@b.com")
                        .param("password", "secret")
                        .param("role", "USER"))      // ← must include
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).registerUser(captor.capture());
        assertEquals("a@b.com", captor.getValue().getEmail());
    }
}
