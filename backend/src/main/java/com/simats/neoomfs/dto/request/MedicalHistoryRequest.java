package com.simats.neoomfs.dto.request;

import lombok.Data;

/**
 * Inbound DTO for medical history (Step 5 of the wizard).
 */
@Data
public class MedicalHistoryRequest {
    private boolean hypertension;
    private boolean diabetes;
    private boolean heartDisease;
    private boolean kidneyDisease;
    private boolean liverDisease;
    private boolean thyroidDisorder;
    private boolean asthma;
    private boolean epilepsy;
    private boolean bloodDisorder;
    private boolean hepatitis;
    private boolean hivPositive;
    private boolean pregnant;
    private String pregnancyTrimester;
    private String otherConditions;
    private String currentMedications;
    private String allergies;
    private String previousSurgeries;
    private String anaestheticComplications;
    private String familyHistory;
    private String socialHistory;
    private String notes;
}
