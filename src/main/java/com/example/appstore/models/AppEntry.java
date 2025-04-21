package com.example.appstore.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "apps")
public class AppEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String appName;

    @Lob
    @Column(nullable = false)
    private byte[] appFile;

    @Lob
    @Column(nullable = false)
    private byte[] metadataFile;

    private LocalDateTime uploadDate;

    public AppEntry() {
    }

    public AppEntry(String appName, byte[] appFile, byte[] metadataFile, LocalDateTime uploadDate) {
        this.appName = appName;
        this.appFile = appFile;
        this.metadataFile = metadataFile;
        this.uploadDate = uploadDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public byte[] getAppFile() {
        return appFile;
    }

    public void setAppFile(byte[] appFile) {
        this.appFile = appFile;
    }

    public byte[] getMetadataFile() {
        return metadataFile;
    }

    public void setMetadataFile(byte[] metadataFile) {
        this.metadataFile = metadataFile;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
