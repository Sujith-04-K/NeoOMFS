package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medications extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "drug_name", nullable = false, length = 150)
    private String drugName;

    @Column(name = "dosage", length = 50)
    private String dosage;

    @Column(name = "frequency", length = 50)
    private String frequency;

    @Column(name = "route", length = 50)
    private String route;

    @Column(name = "indication", length = 200)
    private String indication;

    @Column(name = "is_anticoagulant")
    @Builder.Default
    private Boolean isAnticoagulant = false;

    @Column(name = "is_immunosuppressant")
    @Builder.Default
    private Boolean isImmunosuppressant = false;
}
