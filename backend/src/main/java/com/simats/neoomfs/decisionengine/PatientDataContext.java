package com.simats.neoomfs.decisionengine;

import com.simats.neoomfs.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDataContext {
    private Patient patient;
    private PatientVitals vitals;
    private Radiology radiology;
    private LaboratoryInvestigations laboratory;
    private MedicalHistory medicalHistory;
    private List<Medications> medications;
    private DentalExamination dental;

    @Builder.Default
    private List<String> alerts = new ArrayList<>();
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
    @Builder.Default
    private int totalRiskScore = 0;

    public void addAlert(String alert) {
        if (alert != null && !alert.isBlank()) {
            this.alerts.add(alert);
        }
    }

    public void addRecommendation(String rec) {
        if (rec != null && !rec.isBlank()) {
            this.recommendations.add(rec);
        }
    }

    public void addRiskScore(int score) {
        this.totalRiskScore += score;
    }
}
