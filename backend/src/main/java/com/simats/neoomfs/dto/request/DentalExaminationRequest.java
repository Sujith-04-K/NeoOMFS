package com.simats.neoomfs.dto.request;

import lombok.Data;

/**
 * Inbound DTO for dental examination (Step 6 of the wizard).
 */
@Data
public class DentalExaminationRequest {
    private String asaClass;
    private String pellGregoryClass;
    private String winterClassification;
    private String upperThirdMolar;
    private Integer difficultyScore;
    private Integer mouthOpeningMm;
    private String oralHygieneStatus;
    private String periodontalStatus;
    private boolean activeInfection;
    private boolean swelling;
    private boolean trismus;
    private String toothNumber;
    private String clinicalExaminationNotes;
}
