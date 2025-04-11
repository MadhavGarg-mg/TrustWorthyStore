package com.example.appstore.controllers;

import com.example.appstore.services.AppService;
import com.example.appstore.services.FileValidationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
public class AppUploadController {

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private AppService appService;

    /**
     * Handles file upload for the app and its metadata.
     * Expects:
     *  - metadataFile: a JSON file containing fields like "appName" and "expectedHash".
     *  - appFile: the main application file.
     */
    @PostMapping("/upload")
    public String handleUpload(@RequestParam("metadataFile") MultipartFile metadataFile,
                               @RequestParam("appFile") MultipartFile appFile,
                               Model model) {
        try {
            // Parse metadata JSON to extract expectedHash and appName.
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> metadataMap = mapper.readValue(metadataFile.getBytes(), new TypeReference<Map<String, String>>() {});
            String expectedHash = metadataMap.get("expectedHash");
            String appName = metadataMap.get("appName");

            if (expectedHash == null || appName == null) {
                model.addAttribute("error", "Metadata is missing required fields (expectedHash and appName).");
                return "appUpload";
            }
            expectedHash = expectedHash.trim();

            // Validate the app file using the external checker tool.
            boolean valid = fileValidationService.validateFile(appFile, expectedHash);
            if (!valid) {
                model.addAttribute("error", "File hash validation failed for the app file.");
                return "appUpload";
            }

            // Save the validated app file and metadata into the database.
            appService.saveAppAndMetadata(appName, appFile, metadataFile);

            model.addAttribute("message", "Files uploaded and validated successfully!");
            return "appUpload"; // Return the view named "appUpload"
        } catch (IOException e) {
            model.addAttribute("error", "Error reading metadata: " + e.getMessage());
            return "appUpload";
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "appUpload";
        }
    }
}
