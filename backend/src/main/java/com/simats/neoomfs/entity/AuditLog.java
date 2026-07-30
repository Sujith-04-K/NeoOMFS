package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AuditLog entity – immutable records of significant user actions.
 * Not extending BaseEntity to avoid modification tracking overhead.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_patient", columnList = "patient_id")
        })
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "username", length = 80)
    private String username;

    @Column(name = "patient_id")
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW, GENERATE_REPORT

    @Column(name = "entity_type", length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
