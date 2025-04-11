package com.example.appstore.services;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidationService {

    private final RestTemplate restTemplate;

    public FileValidationService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    /**
     * Validates the uploaded file by sending it to an external SHA256 checker service.
     *
     * @param file the uploaded file
     * @param expectedHash the expected SHA256 hash (hexadecimal string)
     * @return true if the computed hash matches the expected hash; false otherwise
     * @throws IOException if reading file bytes fails
     */
    public boolean validateFile(MultipartFile file, String expectedHash) throws IOException {
        // Prepare multipart form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Wrap the file bytes so the filename is preserved
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", resource);
        body.add("expected", expectedHash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Assuming the checker service is running on the Docker network with hostname "checker" on port 5000.
        String url = "http://checker:5000/check";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object matches = response.getBody().get("matches");
            return Boolean.TRUE.equals(matches);
        }
        return false;
    }
}
