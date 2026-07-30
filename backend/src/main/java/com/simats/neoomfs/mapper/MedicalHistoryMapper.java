package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.response.MedicalHistoryResponse;
import com.simats.neoomfs.entity.MedicalHistory;
import org.springframework.stereotype.Component;

@Component
public class MedicalHistoryMapper {

    public MedicalHistoryResponse toResponse(MedicalHistory history) {
        if (history == null) return null;
        return MedicalHistoryResponse.builder()
                .id(history.getId())
                .patientId(history.getPatient().getId())
                .hypertension(history.isHypertension())
                .diabetes(history.isDiabetes())
                .hepatitis(history.isHepatitis())
                .kidneyDisease(history.isKidneyDisease())
                .heartDisease(history.isHeartDisease())
                .thyroid(history.isThyroidDisorder())
                .asthma(history.isAsthma())
                .epilepsy(history.isEpilepsy())
                .bloodDisorder(history.isBloodDisorder())
                .liverDisease(history.isLiverDisease())
                .pregnancyStatus(history.isPregnant())
                // Parse socialHistory string for smoking/alcohol flags if needed, or default
                .smoking(history.getSocialHistory() != null && history.getSocialHistory().toLowerCase().contains("smoking"))
                .alcoholUse(history.getSocialHistory() != null && history.getSocialHistory().toLowerCase().contains("alcohol"))
                .currentMedications(history.getCurrentMedications())
                .allergies(history.getAllergies())
                .previousSurgeries(history.getPreviousSurgeries())
                .familyHistory(history.getFamilyHistory())
                .otherConditions(history.getOtherConditions())
                .build();
    }
}
