package com.simats.neoomfs.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.neoomfs.dto.response.ClinicalDecisionResponse;
import com.simats.neoomfs.entity.ClinicalDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClinicalDecisionMapper {

    private final ObjectMapper objectMapper;

    public ClinicalDecisionResponse toResponse(ClinicalDecision decision) {
        if (decision == null) return null;

        List<String> alerts = parseJsonList(decision.getClinicalAlerts());
        List<String> recommendations = parseJsonList(decision.getRecommendations());

        return ClinicalDecisionResponse.builder()
                .id(decision.getId())
                .patientId(decision.getPatient().getId())
                .patientName(decision.getPatient().getFullName())
                .patientMrn(decision.getPatient().getMrn())
                .riskLevel(decision.getRiskLevel())
                .fitnessDecision(decision.getFitnessDecision())
                .riskScore(decision.getRiskScore())
                .clinicalAlerts(alerts)
                .recommendations(recommendations)
                .decisionNotes(decision.getDecisionNotes())
                .generatedByName(decision.getGeneratedBy() != null ? decision.getGeneratedBy().getFullName() : "SYSTEM")
                .generatedAt(decision.getGeneratedAt())
                .build();
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse JSON list from DB: {}", json, e);
            return new ArrayList<>();
        }
    }
}
