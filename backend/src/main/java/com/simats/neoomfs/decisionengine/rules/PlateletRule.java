package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import org.springframework.stereotype.Component;

@Component
public class PlateletRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        LaboratoryInvestigations labs = context.getLaboratory();
        if (labs == null) return;

        Integer platelet = labs.getPlateletCount();
        if (platelet != null) {
            if (platelet < 50000) {
                context.addAlert("Severe Thrombocytopenia (Platelets < 50,000 /mcL). Extreme risk of spontaneous bleeding.");
                context.addRecommendation("Absolute contraindication for surgery; request hematologist evaluation and platelet transfusion.");
                context.addRiskScore(40);
            } else if (platelet < 100000) {
                context.addAlert("Moderate Thrombocytopenia (Platelets 50,000 - 100,000 /mcL). Increased surgical bleeding risk.");
                context.addRecommendation("Preoperative clearance required; prepare local hemostatic agents.");
                context.addRiskScore(20);
            }
        }
    }
}
