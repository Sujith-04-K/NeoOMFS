package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BloodSugarRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        LaboratoryInvestigations labs = context.getLaboratory();
        if (labs == null) return;

        BigDecimal fbs = labs.getFastingBloodSugar();
        BigDecimal rbs = labs.getRandomBloodSugar();
        BigDecimal hba1c = labs.getHba1c();

        if (hba1c != null) {
            if (hba1c.compareTo(BigDecimal.valueOf(8.5)) >= 0) {
                context.addAlert("Critically Uncontrolled Diabetes (HbA1c >= 8.5%). High risk of wound infection and poor healing.");
                context.addRecommendation("Defer elective surgery; refer to Diabetologist for glycemic optimization.");
                context.addRiskScore(30);
            } else if (hba1c.compareTo(BigDecimal.valueOf(7.0)) >= 0) {
                context.addAlert("Uncontrolled Diabetes (HbA1c 7.0% - 8.4%). Moderately high surgical risk.");
                context.addRecommendation("Refer to physician for glucose control advice; schedule morning surgery.");
                context.addRiskScore(15);
            }
        }

        if (fbs != null && fbs.compareTo(BigDecimal.valueOf(200)) > 0) {
            context.addAlert("Fasting Blood Sugar highly elevated (> 200 mg/dL). Risk of intraoperative complications.");
            context.addRiskScore(10);
        }
        
        if (rbs != null && rbs.compareTo(BigDecimal.valueOf(250)) > 0) {
            context.addAlert("Random Blood Sugar highly elevated (> 250 mg/dL).");
            context.addRiskScore(10);
        }
    }
}
