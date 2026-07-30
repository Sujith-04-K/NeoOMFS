package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import org.springframework.stereotype.Component;

@Component
public class InfectionRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        LaboratoryInvestigations labs = context.getLaboratory();
        if (labs == null) return;

        String hiv = labs.getHivStatus();
        String hbsag = labs.getHbsagStatus();
        String hcv = labs.getHcvStatus();

        if (hiv != null && hiv.equalsIgnoreCase("POSITIVE")) {
            context.addAlert("HIV Positive Status. High risk of opportunistic post-surgical infection.");
            context.addRecommendation("Ensure standard universal precautions are rigidly enforced. Review CD4 count/viral load prior to surgery.");
            context.addRiskScore(20);
        }

        if (hbsag != null && hbsag.equalsIgnoreCase("POSITIVE")) {
            context.addAlert("Hepatitis B Surface Antigen (HBsAg) Positive. High transmission risk.");
            context.addRecommendation("Observe rigid infection control protocols. Staff protection and immunization verification.");
            context.addRiskScore(15);
        }

        if (hcv != null && hcv.equalsIgnoreCase("POSITIVE")) {
            context.addAlert("Hepatitis C (HCV) Positive. High transmission risk.");
            context.addRecommendation("Rigid barrier protection; check liver enzymes (SGOT/SGPT) and coagulation panel.");
            context.addRiskScore(15);
        }
    }
}
