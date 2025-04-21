package com.example.appstore.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileValidationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private FileValidationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(restTemplate);
        service = new FileValidationService(builder);
        // inject a fake URL
        ReflectionTestUtils.setField(service, "checkerUrl", "http://fake/check");
    }

    private MultipartFile makeFile(String name, byte[] data) throws IOException {
        MultipartFile f = mock(MultipartFile.class);
        when(f.getOriginalFilename()).thenReturn(name);
        when(f.getBytes()).thenReturn(data);
        return f;
    }

    @Test
    void validateFile_returnsTrue_whenCheckerSaysMatch() throws IOException {
        MultipartFile file = makeFile("foo.txt", "hello".getBytes());
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> resp = new ResponseEntity<>(
                Collections.singletonMap("matches", true),
                HttpStatus.OK
        );
        when(restTemplate.postForEntity(
                eq("http://fake/check"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(resp);

        boolean ok = service.validateFile(file, "abc123");
        assertTrue(ok);

        ArgumentCaptor<HttpEntity> cap = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("http://fake/check"), cap.capture(), eq(Map.class));
        HttpEntity<?> sent = cap.getValue();
        assertEquals(MediaType.MULTIPART_FORM_DATA, sent.getHeaders().getContentType());
    }

    @Test
    void validateFile_returnsFalse_onNon2xxOrNoBody() throws IOException {
        MultipartFile file = makeFile("foo.txt", "x".getBytes());
        ResponseEntity<Map> resp500 = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(resp500);
        assertFalse(service.validateFile(file, "doesntmatter"));
    }

    @Test
    void validateFile_returnsFalse_whenMatchesFalse() throws IOException {
        MultipartFile file = makeFile("foo.txt", "x".getBytes());
        ResponseEntity<Map> resp = new ResponseEntity<>(
                Collections.singletonMap("matches", false),
                HttpStatus.OK
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(resp);
        assertFalse(service.validateFile(file, "whatever"));
    }
}
