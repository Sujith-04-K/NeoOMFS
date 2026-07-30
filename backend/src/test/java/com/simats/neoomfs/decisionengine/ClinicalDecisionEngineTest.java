package com.simats.neoomfs.decisionengine;

import com.simats.neoomfs.decisionengine.rules.*;
import com.simats.neoomfs.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClinicalDecisionEngineTest {

    private ClinicalDecisionEngine engine;
    private ASARule asaRule;
    private BPRule bpRule;
    private BloodSugarRule bloodSugarRule;
    private INRRule inrRule;
    private PlateletRule plateletRule;

    @BeforeEach
    public void setUp() {
        asaRule = new ASARule();
        bpRule = new BPRule();
        bloodSugarRule = new BloodSugarRule();
        inrRule = new INRRule();
        plateletRule = new PlateletRule();

        List<ClinicalRule> rules = Arrays.asList(
                asaRule, bpRule, bloodSugarRule, inrRule, plateletRule
        );

        RiskCalculator riskCalculator = new RiskCalculator();
        FitnessEvaluator fitnessEvaluator = new FitnessEvaluator();

        engine = new ClinicalDecisionEngine(rules, riskCalculator, fitnessEvaluator);
    }

    @Test
    @DisplayName("Evaluate healthy patient (ASA I, Normal BP, Normal Labs) -> LOW risk, FIT decision")
    public void testHealthyPatientEvaluation() {
        Patient patient = new Patient();
        patient.setMrn("MRN-1001");
        patient.setFullName("John Doe");

        PatientVitals vitals = new PatientVitals();
        vitals.setBpSystolic(120);
        vitals.setBpDiastolic(80);

        DentalExamination dental = new DentalExamination();
        dental.setAsaClass("ASA_I");

        LaboratoryInvestigations labs = new LaboratoryInvestigations();
        labs.setInr(BigDecimal.valueOf(1.0));
        labs.setHba1c(BigDecimal.valueOf(5.6));
        labs.setPlateletCount(250000);

        PatientDataContext context = PatientDataContext.builder()
                .patient(patient)
                .vitals(vitals)
                .dental(dental)
                .laboratory(labs)
                .build();

        DecisionResult result = engine.evaluate(context);

        assertNotNull(result);
        assertEquals("LOW", result.getRiskLevel());
        assertEquals("FIT", result.getFitnessDecision());
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getAlerts().stream().anyMatch(a -> a.contains("No high-risk clinical alerts identified")));
    }

    @Test
    @DisplayName("Evaluate hypertensive crisis (BP 185/115) + ASA IV -> VERY_HIGH risk, CRITICAL decision")
    public void testCriticalPatientEvaluation() {
        Patient patient = new Patient();
        patient.setMrn("MRN-9999");
        patient.setFullName("Critical Patient");

        PatientVitals vitals = new PatientVitals();
        vitals.setBpSystolic(185);
        vitals.setBpDiastolic(115);

        DentalExamination dental = new DentalExamination();
        dental.setAsaClass("ASA_IV");

        PatientDataContext context = PatientDataContext.builder()
                .patient(patient)
                .vitals(vitals)
                .dental(dental)
                .build();

        DecisionResult result = engine.evaluate(context);

        assertNotNull(result);
        assertEquals("VERY_HIGH", result.getRiskLevel());
        assertEquals("CRITICAL", result.getFitnessDecision());
        assertTrue(result.getRiskScore() >= 80, "Score should be at least 80 (40 BP + 40 ASA)");
        assertTrue(result.getAlerts().stream().anyMatch(a -> a.contains("Hypertensive Crisis")));
        assertTrue(result.getAlerts().stream().anyMatch(a -> a.contains("ASA Class IV")));
    }

    @Test
    @DisplayName("Evaluate diabetic patient with coagulopathy (HbA1c 8.6%, INR 2.8) -> VERY_HIGH risk, CRITICAL decision")
    public void testDiabeticAndCoagulopathyPatientEvaluation() {
        Patient patient = new Patient();
        patient.setMrn("MRN-2002");
        patient.setFullName("Diabetic Coagulopathy Patient");

        LaboratoryInvestigations labs = new LaboratoryInvestigations();
        labs.setHba1c(BigDecimal.valueOf(8.6));
        labs.setInr(BigDecimal.valueOf(2.8));

        PatientDataContext context = PatientDataContext.builder()
                .patient(patient)
                .laboratory(labs)
                .build();

        DecisionResult result = engine.evaluate(context);

        assertNotNull(result);
        assertEquals("VERY_HIGH", result.getRiskLevel());
        assertEquals("CRITICAL", result.getFitnessDecision());
        assertTrue(result.getRiskScore() >= 70, "Score should be 70 (30 HbA1c + 40 INR)");
        assertTrue(result.getAlerts().stream().anyMatch(a -> a.contains("Critically Uncontrolled Diabetes")));
        assertTrue(result.getAlerts().stream().anyMatch(a -> a.contains("Critical Bleeding Risk")));
    }
}
