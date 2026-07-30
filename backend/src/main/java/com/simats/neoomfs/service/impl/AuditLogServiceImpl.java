package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.response.AuditLogResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.entity.AuditLog;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.repository.AuditLogRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void log(Long userId, String username, String action, String entityType, Long entityId, String description) {
        log(userId, username, null, entityType, action, description, entityType, entityId);
    }

    @Override
    public void log(String action, String entityType, Long entityId, String description) {
        log(null, "SYSTEM", null, entityType, action, description, entityType, entityId);
    }

    @Override
    public void log(Long userId, String username, Long patientId, String module, String action, String description, String entityType, Long entityId) {
        String ipAddress = getClientIp();

        if (userId == null && !"SYSTEM".equals(username)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !anonymous(auth)) {
                String email = auth.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    userId = user.getId();
                    username = user.getUsername();
                }
            }
        }

        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .username(username != null ? username : "SYSTEM")
                .patientId(patientId)
                .module(module)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage = auditLogRepository.findAll(pageable);
        return toPagedResponse(auditLogPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogsByPatient(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage = auditLogRepository.findByPatientId(patientId, pageable);
        return toPagedResponse(auditLogPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage = auditLogRepository.findByUserId(userId, pageable);
        return toPagedResponse(auditLogPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogsByModule(String module, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogPage = auditLogRepository.findByModuleIgnoreCase(module, pageable);
        return toPagedResponse(auditLogPage);
    }

    private boolean anonymous(Authentication auth) {
        return auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return "127.0.0.1";
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return AuditLogResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .username(entry.getUsername())
                .patientId(entry.getPatientId())
                .patientName(entry.getPatient() != null ? entry.getPatient().getFullName() : null)
                .patientMrn(entry.getPatient() != null ? entry.getPatient().getMrn() : null)
                .module(entry.getModule())
                .action(entry.getAction())
                .description(entry.getDescription())
                .ipAddress(entry.getIpAddress())
                .timestamp(entry.getTimestamp())
                .build();
    }

    private PagedResponse<AuditLogResponse> toPagedResponse(Page<AuditLog> page) {
        List<AuditLogResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
