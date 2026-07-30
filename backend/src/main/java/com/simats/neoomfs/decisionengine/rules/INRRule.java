package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class INRRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        LaboratoryInvestigations labs = context.getLaboratory();
        if (labs == null) return;

        BigDecimal inr = labs.getInr();
        if (inr != null) {
            if (inr.compareTo(BigDecimal.valueOf(2.5)) > 0) {
                context.addAlert("Critical Bleeding Risk (INR > 2.5). General surgical contraindication.");
                context.addRecommendation("Defer surgery immediately; coordinate with physician to bridge anticoagulants.");
                context.addRiskScore(40);
            } else if (inr.compareTo(BigDecimal.valueOf(1.5)) > 0) {
                context.addAlert("Elevated Bleeding Risk (INR 1.5 - 2.5). Risk of post-extractive hemorrhage.");
                context.addRecommendation("Ensure local hemostatic measures (suturing, Gelfoam) are prepared.");
                context.addRiskScore(15);
            }
        }
    }
}
