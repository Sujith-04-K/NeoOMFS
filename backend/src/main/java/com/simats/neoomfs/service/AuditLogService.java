package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.AuditLogResponse;
import com.simats.neoomfs.dto.response.PagedResponse;

public interface AuditLogService {

    void log(Long userId, String username, String action, String entityType, Long entityId, String description);

    void log(String action, String entityType, Long entityId, String description);

    void log(Long userId, String username, Long patientId, String module, String action, String description, String entityType, Long entityId);

    PagedResponse<AuditLogResponse> getAllAuditLogs(int page, int size);

    PagedResponse<AuditLogResponse> getAuditLogsByPatient(Long patientId, int page, int size);

    PagedResponse<AuditLogResponse> getAuditLogsByUser(Long userId, int page, int size);

    PagedResponse<AuditLogResponse> getAuditLogsByModule(String module, int page, int size);
}
