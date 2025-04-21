package com.example.appstore.controllers;

import com.example.appstore.models.AppEntry;
import com.example.appstore.services.AppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppDownloadControllerTest {

    @InjectMocks
    private AppDownloadController controller;

    @Mock
    private AppService appService;

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
        return MockMvcBuilders.standaloneSetup(ctrl)
                .setViewResolvers(vr)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(),
                        new ByteArrayHttpMessageConverter()
                )
                .build();
    }

    @Test
    void download_existingApp_returnsFile() throws Exception {
        AppEntry entry = new AppEntry();
        entry.setAppName("MyApp");
        entry.setAppFile(new byte[]{1,2,3});
        when(appService.getAppEntry(5L)).thenReturn(entry);

        mvc.perform(get("/app-download/5"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "form-data; name=\"attachment\"; filename=\"MyApp\""
                ))
                .andExpect(content().bytes(new byte[]{1,2,3}));
    }

    @Test
    void download_nonexistentApp_returns404() throws Exception {
        when(appService.getAppEntry(99L)).thenReturn(null);

        mvc.perform(get("/app-download/99"))
                .andExpect(status().isNotFound());
    }
}
