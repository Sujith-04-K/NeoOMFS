package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient entity – core demographics for a preoperative assessment candidate.
 * Each patient has a unique auto-generated Medical Record Number (MRN).
 * Assessment completion is tracked via the status field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "patients",
        uniqueConstraints = @UniqueConstraint(columnNames = "mrn"))
public class Patient extends BaseEntity {

    @Column(name = "mrn", nullable = false, unique = true, length = 20)
    private String mrn;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "procedure_type", length = 100)
    private String procedureType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referring_doctor_id")
    private User referringDoctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_status", length = 20)
    @Builder.Default
    private AssessmentStatus assessmentStatus = AssessmentStatus.DRAFT;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // --- Faculty Review Fields ---

    @Column(name = "submitted_by", length = 150)
    private String submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "review_comments", length = 1000)
    private String reviewComments;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public enum AssessmentStatus {
        DRAFT,
        PENDING_REVIEW,
        APPROVED,
        NEEDS_REVISION
    }
}

