package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Radiology entity – Step 3 of the preoperative wizard.
 * Tracks which radiology modalities were captured and their file storage URLs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "radiology")
public class Radiology extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // IOPA (Intraoral Periapical X-ray)
    @Column(name = "iopa_taken")
    @Builder.Default
    private boolean iopaTaken = false;

    @Column(name = "iopa_file_url", length = 500)
    private String iopaFileUrl;

    @Column(name = "iopa_findings", length = 500)
    private String iopaFindings;

    // OPG (Orthopantomogram)
    @Column(name = "opg_taken")
    @Builder.Default
    private boolean opgTaken = false;

    @Column(name = "opg_file_url", length = 500)
    private String opgFileUrl;

    @Column(name = "opg_findings", length = 500)
    private String opgFindings;

    // CBCT (Cone Beam CT)
    @Column(name = "cbct_taken")
    @Builder.Default
    private boolean cbctTaken = false;

    @Column(name = "cbct_file_url", length = 500)
    private String cbctFileUrl;

    @Column(name = "cbct_findings", length = 500)
    private String cbctFindings;

    // Mandibular bone density estimate (HU from CBCT)
    @Column(name = "bone_density_hu")
    private Double boneDensityHu;

    @Column(name = "general_radiology_notes", length = 1000)
    private String generalRadiologyNotes;
}
