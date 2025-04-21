package com.example.appstore.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginControllerTest {

    private MockMvc buildMvc(Object ctrl) {
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("");
        vr.setSuffix(".html");
        return MockMvcBuilders.standaloneSetup(ctrl)
                .setViewResolvers(vr)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void whenUnauthenticated_returnsLoginView() throws Exception {
        MockMvc mvc = buildMvc(new LoginController());
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void whenAuthenticated_redirectsHome() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", "cred",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        MockMvc mvc = buildMvc(new LoginController());

        mvc.perform(get("/login").principal(auth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void whenAnonymousToken_returnsLoginView() throws Exception {
        List<SimpleGrantedAuthority> auths =
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        Authentication anon = new AnonymousAuthenticationToken(
                "key", "anonymousUser", auths
        );
        MockMvc mvc = buildMvc(new LoginController());

        mvc.perform(get("/login").principal(anon))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}
