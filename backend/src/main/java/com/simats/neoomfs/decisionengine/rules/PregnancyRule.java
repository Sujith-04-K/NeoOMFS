package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.MedicalHistory;
import org.springframework.stereotype.Component;

@Component
public class PregnancyRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        MedicalHistory history = context.getMedicalHistory();
        if (history == null) return;

        if (history.isPregnant()) {
            context.addAlert("Patient is Pregnant. Fetal safety considerations apply.");
            context.addRecommendation("Defer elective surgery until postpartum. For emergency procedures, consult Obstetrician; maximize shielding for dental radiography (double lead apron); select FDA Category B drugs.");
            context.addRiskScore(25);
        }
    }
}
