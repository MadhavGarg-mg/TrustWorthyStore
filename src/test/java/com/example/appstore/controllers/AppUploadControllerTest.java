package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppUploadControllerTest {

    @InjectMocks
    private AppUploadController controller;

    @Mock private FileValidationService fileValidationService;
    @Mock private AppService appService;
    @Mock private UserService userService;
    @Mock private WarningService warningService;

    private MockMvc mvc;
    private User user;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mvc = buildMvc(controller);

        user = new User();
        user.setEmail("test@example.com");
        user.setSuspended(false);
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
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
    void showForm_notSuspended() throws Exception {
        mvc.perform(get("/app-upload").principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("appUpload"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    void upload_missingExpectedHash() throws Exception {
        MockMultipartFile appFile = new MockMultipartFile(
                "appFile","app.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{0});
        MockMultipartFile meta = new MockMultipartFile(
                "metadataFile","meta.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"foo\":\"bar\"}".getBytes());

        mvc.perform(multipart("/app-upload")
                        .file(appFile)
                        .file(meta)
                        .param("appName", "MyApp")       // ← added
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("appUpload"))
                .andExpect(model().attribute("error",
                        "Metadata must include an 'expectedHash' field."));
    }

    @Test
    void upload_hashMismatch_issuesWarning() throws Exception {
        when(fileValidationService.validateFile(any(), anyString())).thenReturn(false);
        when(warningService.issueWarning(user, "Hash mismatch for app 'MyApp'"))
                .thenReturn(1L);

        MockMultipartFile appFile = new MockMultipartFile(
                "appFile","app.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{});
        MockMultipartFile meta = new MockMultipartFile(
                "metadataFile","meta.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"expectedHash\":\"abc\"}".getBytes());

        mvc.perform(multipart("/app-upload")
                        .file(appFile)
                        .file(meta)
                        .param("appName","MyApp")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("appUpload"))
                .andExpect(model().attribute("error", "Hash mismatch. Warning 1/3."));
    }

    @Test
    void upload_success() throws Exception {
        when(fileValidationService.validateFile(any(), anyString())).thenReturn(true);

        MockMultipartFile appFile = new MockMultipartFile(
                "appFile","app.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{});
        MockMultipartFile meta = new MockMultipartFile(
                "metadataFile","meta.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"expectedHash\":\"abc\"}".getBytes());

        mvc.perform(multipart("/app-upload")
                        .file(appFile)
                        .file(meta)
                        .param("appName","MyApp")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("appUpload"))
                .andExpect(model().attribute("message", "Upload and validation succeeded!"));

        verify(appService).saveAppAndMetadata(eq("MyApp"), any(), any());
    }
}
