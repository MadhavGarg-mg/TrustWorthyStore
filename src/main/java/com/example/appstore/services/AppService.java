package com.example.appstore.services;

import com.example.appstore.models.AppEntry;
import com.example.appstore.repository.AppEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class AppService {

    @Autowired
    private AppEntryRepository appEntryRepository;

    /**
     * Saves the uploaded app and metadata files to the database.
     *
     * @param appName the name of the app
     * @param appFile the main application file
     * @param metadataFile the metadata file
     * @return the saved AppEntry entity
     * @throws IOException if reading file bytes fails
     */
    public AppEntry saveAppAndMetadata(String appName, MultipartFile appFile, MultipartFile metadataFile) throws IOException {
        AppEntry entry = new AppEntry();
        entry.setAppName(appName);
        entry.setAppFile(appFile.getBytes());
        entry.setMetadataFile(metadataFile.getBytes());
        entry.setUploadDate(LocalDateTime.now());
        return appEntryRepository.save(entry);
    }
}
