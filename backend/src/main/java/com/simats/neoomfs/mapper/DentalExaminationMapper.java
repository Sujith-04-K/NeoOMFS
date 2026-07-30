package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.response.DentalExaminationResponse;
import com.simats.neoomfs.entity.DentalExamination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DentalExaminationMapper {

    public DentalExaminationResponse toResponse(DentalExamination dental) {
        if (dental == null) return null;
        
        // Compute difficulty level and mouth opening status dynamically for the response
        String difficultyLevel = "MILD";
        if (dental.getDifficultyScore() != null) {
            int score = dental.getDifficultyScore();
            if (score > 7) {
                difficultyLevel = "DIFFICULT";
            } else if (score > 4) {
                difficultyLevel = "MODERATE";
            }
        }

        String mouthOpeningStatus = "NORMAL";
        if (dental.getMouthOpeningMm() != null) {
            int mo = dental.getMouthOpeningMm();
            if (mo < 25) {
                mouthOpeningStatus = "SEVERE_TRISMUS";
            } else if (mo < 40) {
                mouthOpeningStatus = "RESTRICTED";
            }
        }

        return DentalExaminationResponse.builder()
                .id(dental.getId())
                .patientId(dental.getPatient().getId())
                .impactionClass(dental.getPellGregoryClass())
                .winterClassification(dental.getWinterClassification())
                .upperThirdMolarStatus(dental.getUpperThirdMolar())
                .difficultyScore(dental.getDifficultyScore())
                .difficultyLevel(difficultyLevel)
                .asaClass(dental.getAsaClass())
                .periodontalStatus(dental.getPeriodontalStatus())
                .oralHygiene(dental.getOralHygieneStatus())
                .mouthOpeningMm(dental.getMouthOpeningMm())
                .mouthOpeningStatus(mouthOpeningStatus)
                .teethPresent(dental.getToothNumber())
                .clinicalFindings(dental.getClinicalExaminationNotes())
                .build();
    }
}
