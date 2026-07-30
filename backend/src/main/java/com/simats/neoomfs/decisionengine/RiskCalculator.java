package com.simats.neoomfs.decisionengine;

import org.springframework.stereotype.Component;

@Component
public class RiskCalculator {

    public String calculateRiskLevel(int totalRiskScore) {
        if (totalRiskScore >= 50) {
            return "VERY_HIGH";
        } else if (totalRiskScore >= 25) {
            return "HIGH";
        } else if (totalRiskScore >= 10) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }
}
