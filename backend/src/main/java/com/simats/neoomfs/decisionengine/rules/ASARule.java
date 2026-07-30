package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.DentalExamination;
import org.springframework.stereotype.Component;

@Component
public class ASARule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        DentalExamination dental = context.getDental();
        if (dental == null) return;

        String asa = dental.getAsaClass();
        if (asa != null) {
            switch (asa.toUpperCase()) {
                case "ASA_I":
                case "ASA_1":
                case "I":
                case "1":
                    context.addRiskScore(0);
                    break;
                case "ASA_II":
                case "ASA_2":
                case "II":
                case "2":
                    context.addAlert("ASA Class II: Mild systemic disease (controlled). Low to moderate risk.");
                    context.addRiskScore(10);
                    break;
                case "ASA_III":
                case "ASA_3":
                case "III":
                case "3":
                    context.addAlert("ASA Class III: Severe systemic disease with functional limitation. Significant risk.");
                    context.addRecommendation("Mandatory preoperative physician or anesthesiologist clearance.");
                    context.addRiskScore(25);
                    break;
                case "ASA_IV":
                case "ASA_4":
                case "IV":
                case "4":
                    context.addAlert("ASA Class IV: Severe systemic disease that is a constant threat to life.");
                    context.addRecommendation("Contraindicated for office surgery. Must be handled in an inpatient hospital setting.");
                    context.addRiskScore(40);
                    break;
                default:
                    break;
            }
        }
    }
}
