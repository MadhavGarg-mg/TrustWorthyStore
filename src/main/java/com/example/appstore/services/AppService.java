package com.example.appstore.services;

import com.example.appstore.models.AppEntry;
import com.example.appstore.repository.AppEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppService {

    @Autowired
    private AppEntryRepository appEntryRepository;

    public AppEntry saveAppAndMetadata(String appName, MultipartFile appFile, MultipartFile metadataFile) throws IOException {
        AppEntry entry = new AppEntry();
        entry.setAppName(appName);
        entry.setAppFile(appFile.getBytes());
        entry.setMetadataFile(metadataFile.getBytes());
        entry.setUploadDate(LocalDateTime.now());
        return appEntryRepository.save(entry);
    }

    public List<AppEntry> getAllApps() {
        return appEntryRepository.findAll();
    }

    public AppEntry getAppEntry(Long id) {
        return appEntryRepository.findById(id).orElse(null);
    }
}
