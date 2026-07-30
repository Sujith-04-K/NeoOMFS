package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Notification entity – stores in-app notifications for clinical staff.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    @Builder.Default
    private NotificationType type = NotificationType.INFO;

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false;

    @Column(name = "related_patient_id")
    private Long relatedPatientId;

    public enum NotificationType {
        INFO, WARNING, CRITICAL, SUCCESS
    }
}
