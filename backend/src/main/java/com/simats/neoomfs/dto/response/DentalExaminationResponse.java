package com.simats.neoomfs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalExaminationResponse {
    private Long id;
    private Long patientId;
    private String impactionClass;
    private String impactionPosition;
    private String winterClassification;
    private String upperThirdMolarStatus;
    private String lowerThirdMolarStatus;
    private Integer difficultyScore;
    private String difficultyLevel;
    private String asaClass;
    private String periodontalStatus;
    private String oralHygiene;
    private Integer mouthOpeningMm;
    private String mouthOpeningStatus;
    private String teethPresent;
    private String clinicalFindings;
}
