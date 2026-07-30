package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * MedicalHistory entity – Step 5 of the preoperative wizard.
 * Records systemic conditions, medications, allergies, and surgical history.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medical_history")
public class MedicalHistory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // Systemic conditions (boolean flags)
    @Column(name = "hypertension") @Builder.Default private boolean hypertension = false;
    @Column(name = "diabetes")     @Builder.Default private boolean diabetes = false;
    @Column(name = "heart_disease") @Builder.Default private boolean heartDisease = false;
    @Column(name = "kidney_disease") @Builder.Default private boolean kidneyDisease = false;
    @Column(name = "liver_disease") @Builder.Default private boolean liverDisease = false;
    @Column(name = "thyroid_disorder") @Builder.Default private boolean thyroidDisorder = false;
    @Column(name = "asthma")       @Builder.Default private boolean asthma = false;
    @Column(name = "epilepsy")     @Builder.Default private boolean epilepsy = false;
    @Column(name = "blood_disorder") @Builder.Default private boolean bloodDisorder = false;
    @Column(name = "hepatitis")    @Builder.Default private boolean hepatitis = false;
    @Column(name = "hiv_positive") @Builder.Default private boolean hivPositive = false;
    @Column(name = "pregnancy_status") @Builder.Default private boolean pregnant = false;

    // Pregnancy trimesters if pregnant
    @Column(name = "pregnancy_trimester", length = 20)
    private String pregnancyTrimester;

    // Free-text fields
    @Column(name = "other_conditions", length = 500)
    private String otherConditions;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "previous_surgeries", columnDefinition = "TEXT")
    private String previousSurgeries;

    @Column(name = "anaesthetic_complications", length = 500)
    private String anaestheticComplications;

    @Column(name = "family_history", length = 500)
    private String familyHistory;

    @Column(name = "social_history", length = 300)
    private String socialHistory; // smoking, alcohol

    @Column(name = "notes", length = 1000)
    private String notes;
}
