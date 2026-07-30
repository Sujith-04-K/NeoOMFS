package com.simats.neoomfs.storage;

import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.BusinessRuleException;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.AuditLogService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Value("${storage.upload-dir:backend/uploads}")
    private String uploadDirStr;

    private Path uploadDir;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    @PostConstruct
    public void init() {
        this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
            log.info("Initialized file upload directory at: {}", this.uploadDir);
        } catch (IOException e) {
            log.error("Could not create the upload directory: {}", this.uploadDir, e);
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String storeFile(MultipartFile file, String subFolder) {
        String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";

        int i = originalName.lastIndexOf('.');
        if (i > 0) {
            extension = originalName.substring(i);
        }

        // Validate type (JPEG, PNG, PDF, DICOM/dcm)
        String mimeType = file.getContentType();
        if (mimeType != null) {
            if (!mimeType.startsWith("image/") && !mimeType.equals("application/pdf")
                    && !originalName.endsWith(".dcm") && !originalName.endsWith(".DCM")) {
                throw new BusinessRuleException("Unsupported file type. Only images and PDFs are allowed.");
            }
        }

        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetDir = this.uploadDir.resolve(subFolder).normalize();
            if (!targetDir.startsWith(this.uploadDir)) {
                throw new BusinessRuleException("Path traversal attempt detected");
            }
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(fileName).normalize();
            if (!targetPath.startsWith(this.uploadDir)) {
                throw new BusinessRuleException("Path traversal attempt detected");
            }

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File successfully uploaded: {} -> {}", originalName, targetPath);
            String fileUrl = "/files/" + subFolder + "/" + fileName;

            User currentUser = getCurrentUser();
            Long userId = currentUser != null ? currentUser.getId() : null;
            String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
            auditLogService.log(userId, username, null, "FILE_UPLOAD", "UPLOAD", "Uploaded file: " + originalName + " to folder: " + subFolder, "File", null);

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to store file: {}", originalName, e);
            throw new BusinessRuleException("Could not store file. Please try again.");
        }
    }

    public Path loadFile(String subFolder, String fileName) {
        Path targetPath = this.uploadDir.resolve(subFolder).resolve(fileName).normalize();
        if (!targetPath.startsWith(this.uploadDir)) {
            throw new BusinessRuleException("Path traversal attempt detected");
        }
        return targetPath;
    }
}
