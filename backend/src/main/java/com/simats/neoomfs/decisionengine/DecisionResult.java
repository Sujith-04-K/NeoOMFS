package com.simats.neoomfs.decisionengine;

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
public class DecisionResult {
    private String riskLevel;        // LOW, MODERATE, HIGH, VERY_HIGH
    private String fitnessDecision;  // FIT, REVIEW, CRITICAL
    private int riskScore;
    @Builder.Default
    private List<String> alerts = new ArrayList<>();
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
}
