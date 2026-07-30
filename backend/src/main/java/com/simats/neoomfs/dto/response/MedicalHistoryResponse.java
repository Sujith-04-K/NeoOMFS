package com.simats.neoomfs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryResponse {
    private Long id;
    private Long patientId;
    private Boolean hypertension;
    private Boolean diabetes;
    private Boolean hepatitis;
    private Boolean kidneyDisease;
    private Boolean heartDisease;
    private Boolean thyroid;
    private Boolean asthma;
    private Boolean epilepsy;
    private Boolean bloodDisorder;
    private Boolean liverDisease;
    private Boolean pregnancyStatus;
    private Boolean smoking;
    private Boolean alcoholUse;
    private String currentMedications;
    private String allergies;
    private String previousSurgeries;
    private String familyHistory;
    private String otherConditions;
}
