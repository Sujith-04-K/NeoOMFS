package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * DentalExamination entity – Step 6 of the preoperative wizard.
 * Records Pell-Gregory, Winter, impaction classifications, and oral health status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dental_examination")
public class DentalExamination extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // ASA Classification (I – VI)
    @Column(name = "asa_class", length = 20)
    private String asaClass;

    // Pell-Gregory Classification
    @Column(name = "pell_gregory_class", length = 100)
    private String pellGregoryClass;

    // Winter's Classification
    @Column(name = "winter_classification", length = 100)
    private String winterClassification;

    // Upper Third Molar Status
    @Column(name = "upper_third_molar", length = 100)
    private String upperThirdMolar;

    // Difficulty Score
    @Column(name = "difficulty_score")
    private Integer difficultyScore;

    // Mouth Opening (mm)
    @Column(name = "mouth_opening_mm")
    private Integer mouthOpeningMm;

    // Oral Hygiene Status
    @Column(name = "oral_hygiene_status", length = 100)
    private String oralHygieneStatus;

    // Periodontal Status
    @Column(name = "periodontal_status", length = 255)
    private String periodontalStatus;

    // Clinical Findings
    @Column(name = "active_infection")
    @Builder.Default
    private boolean activeInfection = false;

    @Column(name = "swelling")
    @Builder.Default
    private boolean swelling = false;

    @Column(name = "trismus")
    @Builder.Default
    private boolean trismus = false;

    // Tooth Number
    @Column(name = "tooth_number", length = 20)
    private String toothNumber;

    // Clinical Examination Notes
    @Column(name = "clinical_examination_notes", columnDefinition = "TEXT")
    private String clinicalExaminationNotes;
}