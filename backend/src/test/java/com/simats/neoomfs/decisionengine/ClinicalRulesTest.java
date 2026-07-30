package com.simats.neoomfs.decisionengine;

import com.simats.neoomfs.decisionengine.rules.*;
import com.simats.neoomfs.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ClinicalRulesTest {

    @Test
    @DisplayName("BPRule: Should trigger Hypertensive Crisis alert for BP 180/110")
    public void testBPRuleHypertensiveCrisis() {
        BPRule bpRule = new BPRule();
        PatientVitals vitals = new PatientVitals();
        vitals.setBpSystolic(180);
        vitals.setBpDiastolic(110);

        PatientDataContext context = PatientDataContext.builder()
                .vitals(vitals)
                .build();

        bpRule.evaluate(context);

        assertEquals(40, context.getTotalRiskScore());
        assertTrue(context.getAlerts().stream().anyMatch(a -> a.contains("Hypertensive Crisis")));
        assertTrue(context.getRecommendations().stream().anyMatch(r -> r.contains("Cardiology")));
    }

    @Test
    @DisplayName("ASARule: Should trigger ASA Class III warning and score 25")
    public void testASARuleClassIII() {
        ASARule asaRule = new ASARule();
        DentalExamination dental = new DentalExamination();
        dental.setAsaClass("ASA_III");

        PatientDataContext context = PatientDataContext.builder()
                .dental(dental)
                .build();

        asaRule.evaluate(context);

        assertEquals(25, context.getTotalRiskScore());
        assertTrue(context.getAlerts().stream().anyMatch(a -> a.contains("ASA Class III")));
        assertTrue(context.getRecommendations().stream().anyMatch(r -> r.contains("clearance")));
    }

    @Test
    @DisplayName("INRRule: Should trigger critical bleeding risk for INR > 2.5")
    public void testINRRuleCritical() {
        INRRule inrRule = new INRRule();
        LaboratoryInvestigations labs = new LaboratoryInvestigations();
        labs.setInr(BigDecimal.valueOf(3.1));

        PatientDataContext context = PatientDataContext.builder()
                .laboratory(labs)
                .build();

        inrRule.evaluate(context);

        assertEquals(40, context.getTotalRiskScore());
        assertTrue(context.getAlerts().stream().anyMatch(a -> a.contains("Critical Bleeding Risk")));
    }

    @Test
    @DisplayName("BloodSugarRule: Should trigger uncontrolled diabetes alert for HbA1c >= 8.5%")
    public void testBloodSugarRuleCritical() {
        BloodSugarRule rule = new BloodSugarRule();
        LaboratoryInvestigations labs = new LaboratoryInvestigations();
        labs.setHba1c(BigDecimal.valueOf(9.2));
        labs.setFastingBloodSugar(BigDecimal.valueOf(220));

        PatientDataContext context = PatientDataContext.builder()
                .laboratory(labs)
                .build();

        rule.evaluate(context);

        assertEquals(40, context.getTotalRiskScore()); // 30 (HbA1c >= 8.5) + 10 (FBS > 200)
        assertTrue(context.getAlerts().stream().anyMatch(a -> a.contains("Critically Uncontrolled Diabetes")));
    }

    @Test
    @DisplayName("RiskCalculator: Check risk level boundaries (LOW, MODERATE, HIGH, VERY_HIGH)")
    public void testRiskCalculatorBoundaries() {
        RiskCalculator calculator = new RiskCalculator();

        assertEquals("LOW", calculator.calculateRiskLevel(0));
        assertEquals("LOW", calculator.calculateRiskLevel(9));
        assertEquals("MODERATE", calculator.calculateRiskLevel(10));
        assertEquals("MODERATE", calculator.calculateRiskLevel(24));
        assertEquals("HIGH", calculator.calculateRiskLevel(25));
        assertEquals("HIGH", calculator.calculateRiskLevel(49));
        assertEquals("VERY_HIGH", calculator.calculateRiskLevel(50));
        assertEquals("VERY_HIGH", calculator.calculateRiskLevel(100));
    }

    @Test
    @DisplayName("FitnessEvaluator: Check fitness decisions (FIT, REVIEW, CRITICAL)")
    public void testFitnessEvaluatorBoundaries() {
        FitnessEvaluator evaluator = new FitnessEvaluator();

        assertEquals("FIT", evaluator.evaluateFitness(0, "LOW"));
        assertEquals("REVIEW", evaluator.evaluateFitness(10, "MODERATE"));
        assertEquals("REVIEW", evaluator.evaluateFitness(30, "HIGH"));
        assertEquals("CRITICAL", evaluator.evaluateFitness(45, "HIGH"));
        assertEquals("CRITICAL", evaluator.evaluateFitness(55, "VERY_HIGH"));
    }
}
