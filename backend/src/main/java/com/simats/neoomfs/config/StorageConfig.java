package com.simats.neoomfs.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class StorageConfig {

    @Value("${storage.upload-dir:backend/uploads}")
    private String uploadDir;

    @Value("${storage.reports-dir:backend/reports}")
    private String reportsDir;

    @PostConstruct
    public void createDirectories() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path reportsPath = Paths.get(reportsDir).toAbsolutePath().normalize();

            Files.createDirectories(uploadPath);
            Files.createDirectories(reportsPath);

            log.info("Created local uploads directory at: {}", uploadPath);
            log.info("Created local reports directory at: {}", reportsPath);
        } catch (IOException e) {
            log.error("Could not create directories for file storage", e);
            throw new RuntimeException("Could not initialize file storage directories", e);
        }
    }
}
