package com.example.appstore.controllers;

import com.example.appstore.models.AppEntry;
import com.example.appstore.services.AppService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class AppDownloadController {

    @Autowired
    private AppService appService;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/app-download")
    public String showDownloadPage(
            @RequestParam(value="search", required=false) String search,
            Model model
    ) throws IOException {
        List<AppEntry> all = appService.getAllApps();
        List<Map<String,Object>> viewApps = new ArrayList<>();
        for (AppEntry e : all) {
            Map<String, ?> metadataMap = mapper.readValue(
                    e.getMetadataFile(),
                    new TypeReference<Map<String, ?>>() {}
            );
            Map<String,Object> row = new HashMap<>();
            row.put("id",           e.getId());
            row.put("appName",      e.getAppName());
            row.put("uploadDate",   e.getUploadDate());
            row.put("expectedHash", metadataMap.get("expectedHash"));
            viewApps.add(row);
        }
        if (search != null && !search.isBlank()) {
            String term = search.toLowerCase();
            viewApps.removeIf(m ->
                    !m.get("appName").toString().toLowerCase().contains(term)
            );
        }
        model.addAttribute("apps",   viewApps);
        model.addAttribute("search", search);
        return "appDownload";
    }

    @GetMapping("/app-download/{id}")
    public ResponseEntity<byte[]> downloadApp(@PathVariable Long id) {
        AppEntry e = appService.getAppEntry(id);
        if (e == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", e.getAppName());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).body(e.getAppFile());
    }

    @GetMapping("/app-download/{id}/metadata")
    public ResponseEntity<byte[]> viewMetadata(@PathVariable Long id) {
        AppEntry e = appService.getAppEntry(id);
        if (e == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(e.getMetadataFile());
    }
}
