package com.simats.neoomfs.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Inbound DTO for patient vitals (Step 2 of the wizard).
 */
@Data
public class PatientVitalsRequest {

    @DecimalMin(value = "40", message = "Systolic BP must be at least 40")
    @DecimalMax(value = "300", message = "Systolic BP cannot exceed 300")
    private Integer bpSystolic;

    @DecimalMin(value = "20", message = "Diastolic BP must be at least 20")
    @DecimalMax(value = "200", message = "Diastolic BP cannot exceed 200")
    private Integer bpDiastolic;

    private BigDecimal temperature;
    private Integer pulseRate;
    private BigDecimal spo2;
    private Integer respiratoryRate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private BigDecimal randomBloodSugar;
    private String notes;
}
