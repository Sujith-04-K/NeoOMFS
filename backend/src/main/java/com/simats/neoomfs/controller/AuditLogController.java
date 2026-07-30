package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.AuditLogResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints for viewing and filtering system audit trails")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_FACULTY','ROLE_DOCTOR')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Get all audit logs")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<AuditLogResponse> result =
                auditLogService.getAllAuditLogs(page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully", result)
        );
    }

    @Operation(summary = "Get audit logs for a specific patient")
    @GetMapping("/patient/{id}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogsByPatient(
            @PathVariable("id") Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<AuditLogResponse> result =
                auditLogService.getAuditLogsByPatient(patientId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Patient audit logs retrieved successfully", result)
        );
    }

    @Operation(summary = "Get audit logs for a specific user")
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogsByUser(
            @PathVariable("id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<AuditLogResponse> result =
                auditLogService.getAuditLogsByUser(userId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("User audit logs retrieved successfully", result)
        );
    }

    @Operation(summary = "Get audit logs for a specific module")
    @GetMapping("/module/{module}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogsByModule(
            @PathVariable String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<AuditLogResponse> result =
                auditLogService.getAuditLogsByModule(module, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Module audit logs retrieved successfully", result)
        );
    }
}