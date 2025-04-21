package com.example.appstore.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class HomeControllerTest {

    @Test
    void homeReturnsHomeView() throws Exception {
        HomeController ctrl = new HomeController();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(ctrl).build();

        mvc.perform(get("/"))
                .andExpect(view().name("home"));
    }
}
