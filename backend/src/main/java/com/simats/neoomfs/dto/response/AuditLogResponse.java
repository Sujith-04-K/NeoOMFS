package com.simats.neoomfs.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String module;
    private String action;
    private String description;
    private String ipAddress;
    private LocalDateTime timestamp;
}
