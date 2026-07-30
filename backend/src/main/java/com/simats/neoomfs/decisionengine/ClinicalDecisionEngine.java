package com.simats.neoomfs.decisionengine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicalDecisionEngine {

    private final List<ClinicalRule> rules;
    private final RiskCalculator riskCalculator;
    private final FitnessEvaluator fitnessEvaluator;

    public DecisionResult evaluate(PatientDataContext context) {
        log.info("Running Clinical Decision Engine for Patient MRN: {}", context.getPatient().getMrn());

        // Execute all rules
        for (ClinicalRule rule : rules) {
            try {
                rule.evaluate(context);
            } catch (Exception e) {
                log.error("Error evaluating rule: {}", rule.getClass().getSimpleName(), e);
            }
        }

        String riskLevel = riskCalculator.calculateRiskLevel(context.getTotalRiskScore());
        String fitnessDecision = fitnessEvaluator.evaluateFitness(context.getTotalRiskScore(), riskLevel);

        // Add a default low risk message if no alerts are triggered
        if (context.getAlerts().isEmpty()) {
            context.addAlert("No high-risk clinical alerts identified. Proceed under routine preoperative protocols.");
            context.addRecommendation("Standard dental preoperative guidance. Routine monitoring.");
        }

        return DecisionResult.builder()
                .riskLevel(riskLevel)
                .fitnessDecision(fitnessDecision)
                .riskScore(context.getTotalRiskScore())
                .alerts(context.getAlerts())
                .recommendations(context.getRecommendations())
                .build();
    }
}
