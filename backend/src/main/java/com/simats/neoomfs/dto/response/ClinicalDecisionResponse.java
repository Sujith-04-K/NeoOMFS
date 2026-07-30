package com.simats.neoomfs.dto.response;

import com.simats.neoomfs.entity.ClinicalDecision;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clinical decision response DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalDecisionResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private ClinicalDecision.RiskLevel riskLevel;
    private ClinicalDecision.FitnessDecision fitnessDecision;
    private Integer riskScore;
    private List<String> clinicalAlerts;
    private List<String> recommendations;
    private String decisionNotes;
    private String generatedByName;
    private LocalDateTime generatedAt;
}
