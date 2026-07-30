package com.simats.neoomfs.decisionengine.rules;

import com.simats.neoomfs.decisionengine.ClinicalRule;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.entity.MedicalHistory;
import com.simats.neoomfs.entity.Medications;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicationRule implements ClinicalRule {

    @Override
    public void evaluate(PatientDataContext context) {
        // 1. Audit structured medications (extensibility support)
        List<Medications> meds = context.getMedications();
        if (meds != null) {
            for (Medications med : meds) {
                if (Boolean.TRUE.equals(med.getIsAnticoagulant())) {
                    context.addAlert("Anticoagulant Therapy Active (" + med.getDrugName() + "). Elevated risk of intraoperative bleeding.");
                    context.addRecommendation("Verify PT/INR status before extraction; prepare local hemostatics.");
                    context.addRiskScore(15);
                }
                if (Boolean.TRUE.equals(med.getIsImmunosuppressant())) {
                    context.addAlert("Immunosuppressant Therapy Active (" + med.getDrugName() + "). Increased vulnerability to postoperative infection.");
                    context.addRecommendation("Consider prophylactic antibiotic regimen under surgical protocol guidance.");
                    context.addRiskScore(15);
                }
            }
        }

        // 2. Audit free-text currentMedications string inside MedicalHistory (pragmatic alignment with Android frontend)
        MedicalHistory history = context.getMedicalHistory();
        if (history != null && history.getCurrentMedications() != null) {
            String text = history.getCurrentMedications().toLowerCase();

            // Anticoagulants / Antiplatelets keywords
            if (text.contains("aspirin") || text.contains("warfarin") || text.contains("clopidogrel") 
                    || text.contains("heparin") || text.contains("apixaban") || text.contains("eliquis") 
                    || text.contains("rivaroxaban") || text.contains("xarelto") || text.contains("plavix")
                    || text.contains("ecospirin")) {
                context.addAlert("Anticoagulant / Antiplatelet agent detected in history: " + history.getCurrentMedications());
                context.addRecommendation("Verify current coagulation metrics (PT/INR) prior to surgery; prepare localized hemostatic aids (gelfoam/sutures).");
                context.addRiskScore(15);
            }

            // Immunosuppressants / Steroids keywords
            if (text.contains("prednisone") || text.contains("prednisolone") || text.contains("methotrexate") 
                    || text.contains("cyclosporine") || text.contains("azathioprine") || text.contains("dexona")
                    || text.contains("dexamethasone") || text.contains("steroid")) {
                context.addAlert("Immunosuppressant / Steroid agent detected in history: " + history.getCurrentMedications());
                context.addRecommendation("Elevated infection risks and potential adrenal suppression risk. Consult physician; consider pre-op antibiotic prophylaxis.");
                context.addRiskScore(15);
            }
        }
    }
}
