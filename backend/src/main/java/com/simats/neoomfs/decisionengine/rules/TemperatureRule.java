package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.PatientVitals;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TemperatureRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        PatientVitals vitals = context.getVitals();
        if (vitals == null) return;

        BigDecimal temp = vitals.getTemperature();
        if (temp != null) {
            // Assume input is in Celsius. Over 38.0°C is fever.
            if (temp.compareTo(BigDecimal.valueOf(38.0)) >= 0) {
                context.addAlert("Active Pyrexia (Temp >= 38.0°C / 100.4°F). Potential systemic infection.");
                context.addRecommendation("Postpone elective procedure until source of infection is diagnosed and treated.");
                context.addRiskScore(25);
            }
        }
    }
}
