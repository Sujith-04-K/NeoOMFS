package com.simats.neoomfs.decisionengine;

import org.springframework.stereotype.Component;

@Component
public class FitnessEvaluator {

    public String evaluateFitness(int totalRiskScore, String riskLevel) {
        if (totalRiskScore >= 40 || "VERY_HIGH".equals(riskLevel)) {
            return "CRITICAL";
        } else if (totalRiskScore >= 10 || "HIGH".equals(riskLevel) || "MODERATE".equals(riskLevel)) {
            return "REVIEW";
        } else {
            return "FIT";
        }
    }
}
