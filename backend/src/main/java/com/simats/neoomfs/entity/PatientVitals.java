package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * PatientVitals entity – Step 2 of the preoperative wizard.
 * Stores all clinical vital signs for risk assessment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "patient_vitals")
public class PatientVitals extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // Blood Pressure
    @Column(name = "bp_systolic")
    private Integer bpSystolic;

    @Column(name = "bp_diastolic")
    private Integer bpDiastolic;

    // Temperature in Celsius
    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;

    // Pulse Rate (bpm)
    @Column(name = "pulse_rate")
    private Integer pulseRate;

    // SpO2 (%)
    @Column(name = "spo2", precision = 4, scale = 1)
    private BigDecimal spo2;

    // Respiratory Rate (breaths/min)
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    // Anthropometry
    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    // Random Blood Sugar (mg/dL) – preliminary check
    @Column(name = "random_blood_sugar", precision = 5, scale = 1)
    private BigDecimal randomBloodSugar;

    @Column(name = "notes", length = 500)
    private String notes;
}
