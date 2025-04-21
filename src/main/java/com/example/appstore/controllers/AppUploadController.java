package com.example.appstore.controllers;

import com.example.appstore.models.User;
import com.example.appstore.services.AppService;
import com.example.appstore.services.FileValidationService;
import com.example.appstore.services.UserService;
import com.example.appstore.services.WarningService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
public class AppUploadController {

    @Autowired private FileValidationService fileValidationService;
    @Autowired private AppService            appService;
    @Autowired private UserService           userService;
    @Autowired private WarningService        warningService;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/app-upload")
    public String showUploadForm(Model model, Authentication auth) {
        User current = userService.getUserByEmail(auth.getName());
        if (current.isSuspended()) {
            model.addAttribute("error",
                    "Your account is suspended. Contact an admin to reactivate.");
            return "appUpload";
        }
        return "appUpload";
    }

    @PostMapping("/app-upload")
    public String handleUpload(@RequestParam("appName")     String appName,
                               @RequestParam("appFile")     MultipartFile appFile,
                               @RequestParam("metadataFile") MultipartFile metadataFile,
                               Model model,
                               Authentication auth) {
        User current = userService.getUserByEmail(auth.getName());
        if (current.isSuspended()) {
            model.addAttribute("error",
                    "Your account is suspended. Contact an admin to reactivate.");
            return "appUpload";
        }

        try {
            Map<String, String> metadataMap = mapper.readValue(
                    metadataFile.getBytes(),
                    new TypeReference<>() {}
            );
            String expectedHash = metadataMap.get("expectedHash");
            if (expectedHash == null) {
                model.addAttribute("error",
                        "Metadata must include an 'expectedHash' field.");
                return "appUpload";
            }

            boolean valid = fileValidationService.validateFile(appFile, expectedHash);
            if (!valid) {
                // record a warning and get total count
                long count = warningService.issueWarning(current,
                        "Hash mismatch for app '" + appName + "'");
                if (current.isSuspended()) {
                    model.addAttribute("error",
                            "Hash mismatch. You have received 3 warnings and are now suspended.");
                } else {
                    model.addAttribute("error",
                            "Hash mismatch. Warning " + count + "/3.");
                }
                return "appUpload";
            }

            // success → save
            appService.saveAppAndMetadata(appName, appFile, metadataFile);
            model.addAttribute("message", "Upload and validation succeeded!");
            return "appUpload";

        } catch (Exception e) {
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            return "appUpload";
        }
    }
}
