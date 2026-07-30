package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.PatientVitals;
import org.springframework.stereotype.Component;

@Component
public class BPRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        PatientVitals vitals = context.getVitals();
        if (vitals == null) return;

        Integer sbp = vitals.getBpSystolic();
        Integer dbp = vitals.getBpDiastolic();

        if (sbp != null && dbp != null) {
            if (sbp >= 180 || dbp >= 110) {
                context.addAlert("Hypertensive Crisis (BP >= 180/110 mmHg). Immediate medical stabilization required.");
                context.addRecommendation("Defer elective surgery immediately; refer to Cardiology for crisis management.");
                context.addRiskScore(40);
            } else if (sbp >= 160 || dbp >= 100) {
                context.addAlert("Stage 2 Hypertension (BP >= 160/100 mmHg). Elevated cardiovascular risk.");
                context.addRecommendation("Preoperative physician clearance required; optimize antihypertensive therapy.");
                context.addRiskScore(20);
            } else if (sbp >= 140 || dbp >= 90) {
                context.addAlert("Stage 1 Hypertension (BP >= 140/90 mmHg). Monitor intraoperatively.");
                context.addRecommendation("Continue home antihypertensive medications on day of surgery.");
                context.addRiskScore(5);
            }
        }
    }
}
