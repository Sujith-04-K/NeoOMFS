package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Upload a file")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<String>> uploadFile(

            @Parameter(description = "Folder name", example = "radiology")
            @RequestParam(defaultValue = "radiology")
            String folder,

            @Parameter(
                    description = "File to upload",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file")
            MultipartFile file
    ) {

        String fileUrl = fileStorageService.storeFile(file, folder);

        return ResponseEntity.ok(
        ApiResponse.success("File uploaded successfully", fileUrl)
);
    }

    @Operation(summary = "Download/View uploaded file")
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        try {

            Path filePath = fileStorageService.loadFile(folder, filename);

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }

            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {

            log.error("Unable to serve file {}", filename, e);

            return ResponseEntity.notFound().build();
        }
    }
}