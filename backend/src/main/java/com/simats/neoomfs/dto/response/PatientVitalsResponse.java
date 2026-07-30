package com.simats.neoomfs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientVitalsResponse {
    private Long id;
    private Long patientId;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Integer pulseRate;
    private BigDecimal temperature;
    private BigDecimal spo2;
    private Integer respiratoryRate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private BigDecimal randomBloodSugar;
    private String notes;
}
