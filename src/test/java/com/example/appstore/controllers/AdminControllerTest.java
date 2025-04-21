package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.services.UserService;
import com.example.appstore.services.WarningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    @InjectMocks
    private AdminController controller;

    @Mock
    private UserService userService;
    @Mock
    private WarningService warningService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mvc = buildMvc(controller);
    }

    private MockMvc buildMvc(Object ctrl) {
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("");
        vr.setSuffix(".html");
        MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter();
        return MockMvcBuilders.standaloneSetup(ctrl)
                .setViewResolvers(vr)
                .setMessageConverters(jackson)
                .build();
    }

    @Test
    void dashboardAddsEmptyUserAndReturnsAdminView() throws Exception {
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void addAdmin_existingEmailShowsErrorFlash() throws Exception {
        when(userService.emailExists("a@b")).thenReturn(true);

        mvc.perform(post("/admin/addAdmin")
                        .param("email", "a@b")
                        .param("password", "pw"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attribute("error", "Email already in use"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void addAdmin_newEmailRegistersAndShowsMessage() throws Exception {
        when(userService.emailExists("x@y")).thenReturn(false);

        mvc.perform(post("/admin/addAdmin")
                        .param("email", "x@y")
                        .param("password", "pw"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attribute("message", "Admin added successfully"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).registerUser(captor.capture());
        assertEquals("ADMIN", captor.getValue().getRole());
    }

    @Test
    void getUsersData_returnsJsonList() throws Exception {
        User u1 = new User();
        // your User model has no public setId, so use ReflectionTestUtils:
        ReflectionTestUtils.setField(u1, "id", 1L);
        u1.setEmail("e1");
        u1.setRole("USER");
        u1.setSuspended(false);

        User u2 = new User();
        ReflectionTestUtils.setField(u2, "id", 2L);
        u2.setEmail("e2");
        u2.setRole("ADMIN");
        u2.setSuspended(true);

        when(userService.getAllUsers()).thenReturn(Arrays.asList(u1, u2));

        mvc.perform(get("/admin/users/data")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("e1"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].suspended").value(true));
    }

    @Test
    void manageSuspensionsShowsOnlySuspended() throws Exception {
        User s1 = new User(); s1.setSuspended(true);
        User s2 = new User(); s2.setSuspended(false);
        when(userService.getAllUsers()).thenReturn(Arrays.asList(s1, s2));

        mvc.perform(get("/admin/users/manageSuspensions"))
                .andExpect(status().isOk())
                .andExpect(view().name("manageSuspensions"))
                .andExpect(model().attribute("users", Collections.singletonList(s1)));
    }

    @Test
    void unsuspendUser_successClearsAndSaves() throws Exception {
        User u = new User(); u.setEmail("foo"); u.setSuspended(true);
        ReflectionTestUtils.setField(u, "id", 5L);
        when(userService.getUserById(5L)).thenReturn(u);

        mvc.perform(post("/admin/user/5/unsuspend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/manageSuspensions"))
                .andExpect(flash().attribute("message",
                        "User foo has been reactivated and warnings cleared."));

        verify(warningService).clearWarnings(u);
        verify(userService).save(u);
        assertFalse(u.isSuspended());
    }

    @Test
    void unsuspendUser_notFoundOrNotSuspendedShowsError() throws Exception {
        when(userService.getUserById(99L)).thenReturn(null);

        mvc.perform(post("/admin/user/99/unsuspend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/manageSuspensions"))
                .andExpect(flash().attribute("error",
                        "User not found or not currently suspended."));
    }
}
