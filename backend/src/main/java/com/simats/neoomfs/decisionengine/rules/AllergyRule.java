package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.MedicalHistory;
import org.springframework.stereotype.Component;

@Component
public class AllergyRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        MedicalHistory history = context.getMedicalHistory();
        if (history == null || history.getAllergies() == null) return;

        String allergies = history.getAllergies().toLowerCase();
        if (allergies.contains("penicillin") || allergies.contains("amoxicillin")) {
            context.addAlert("Documented Penicillin / Amoxicillin Allergy. Risk of anaphylaxis.");
            context.addRecommendation("Substitute post-op or prophylactic antibiotics with Clindamycin or Erythromycin if indicated.");
            context.addRiskScore(10);
        }
        if (allergies.contains("nsaid") || allergies.contains("aspirin") || allergies.contains("ibuprofen")) {
            context.addAlert("Documented NSAID / Aspirin Allergy.");
            context.addRecommendation("Use alternative analgesics such as Acetaminophen/Paracetamol for post-op pain management.");
            context.addRiskScore(5);
        }
        if (allergies.contains("latex")) {
            context.addAlert("Documented Latex Allergy. Strict avoidance required.");
            context.addRecommendation("Ensure use of non-latex gloves and dental dams during the entire procedure.");
            context.addRiskScore(5);
        }
    }
}
