package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * LaboratoryInvestigations entity – Step 4 of the preoperative wizard.
 * Contains haematological and biochemical parameters required for surgical clearance.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "laboratory_investigations")
public class LaboratoryInvestigations extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // Haematology
    @Column(name = "hemoglobin", precision = 4, scale = 1)
    private BigDecimal hemoglobin; // g/dL

    @Column(name = "total_wbc_count")
    private Integer totalWbcCount; // cells/mm³

    @Column(name = "platelet_count")
    private Integer plateletCount; // thousands/mm³

    @Column(name = "bleeding_time", precision = 3, scale = 1)
    private BigDecimal bleedingTime; // minutes

    @Column(name = "clotting_time", precision = 3, scale = 1)
    private BigDecimal clottingTime; // minutes

    // Coagulation
    @Column(name = "pt", precision = 4, scale = 1)
    private BigDecimal pt; // Prothrombin Time (seconds)

    @Column(name = "inr", precision = 3, scale = 1)
    private BigDecimal inr; // International Normalised Ratio

    @Column(name = "aptt", precision = 4, scale = 1)
    private BigDecimal aptt; // Activated Partial Thromboplastin Time

    // Blood Sugar
    @Column(name = "fasting_blood_sugar", precision = 5, scale = 1)
    private BigDecimal fastingBloodSugar; // mg/dL

    @Column(name = "random_blood_sugar", precision = 5, scale = 1)
    private BigDecimal randomBloodSugar; // mg/dL

    @Column(name = "hba1c", precision = 3, scale = 1)
    private BigDecimal hba1c; // %

    // Renal
    @Column(name = "blood_urea", precision = 5, scale = 1)
    private BigDecimal bloodUrea; // mg/dL

    @Column(name = "serum_creatinine", precision = 4, scale = 2)
    private BigDecimal serumCreatinine; // mg/dL

    // Liver
    @Column(name = "serum_bilirubin_total", precision = 4, scale = 1)
    private BigDecimal serumBilirubinTotal;

    @Column(name = "sgot", precision = 5, scale = 1)
    private BigDecimal sgot; // U/L

    @Column(name = "sgpt", precision = 5, scale = 1)
    private BigDecimal sgpt; // U/L

    // Blood Group
    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "rh_factor", length = 10)
    private String rhFactor;

    // Serology
    @Column(name = "hiv_status", length = 20)
    private String hivStatus; // REACTIVE / NON-REACTIVE

    @Column(name = "hbsag_status", length = 20)
    private String hbsagStatus; // REACTIVE / NON-REACTIVE

    @Column(name = "hcv_status", length = 20)
    private String hcvStatus; // REACTIVE / NON-REACTIVE

    // Optional lab report file
    @Column(name = "lab_report_file_url", length = 500)
    private String labReportFileUrl;

    @Column(name = "notes", length = 500)
    private String notes;
}
