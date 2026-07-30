package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.response.AnalyticsResponse;
import com.simats.neoomfs.entity.ClinicalDecision;
import com.simats.neoomfs.repository.AssessmentReportRepository;
import com.simats.neoomfs.repository.ClinicalDecisionRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PatientRepository patientRepository;
    private final ClinicalDecisionRepository clinicalDecisionRepository;
    private final AssessmentReportRepository assessmentReportRepository;

    @Override
    public AnalyticsResponse getAnalytics() {
        long totalPatients = patientRepository.countByDeletedFalse();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long patientsThisMonth = patientRepository.countByCreatedAtAfter(startOfMonth);

        long reportsGenerated = assessmentReportRepository.count();

        Double avgAgeObj = patientRepository.getAverageAge();
        double averageAge = avgAgeObj != null ? avgAgeObj : 0.0;

        long maleCount = patientRepository.countByGenderIgnoreCaseAndDeletedFalse("male");
        long femaleCount = patientRepository.countByGenderIgnoreCaseAndDeletedFalse("female");

        String maleFemaleRatio;
        if (femaleCount == 0) {
            maleFemaleRatio = femaleCount == 0 && maleCount == 0 ? "0.00" : String.format("%d:0", maleCount);
        } else {
            double ratio = (double) maleCount / femaleCount;
            maleFemaleRatio = String.format("%.2f", ratio);
        }

        // Risk Distribution
        Map<String, Long> riskDistribution = new HashMap<>();
        for (ClinicalDecision.RiskLevel risk : ClinicalDecision.RiskLevel.values()) {
            riskDistribution.put(risk.name(), clinicalDecisionRepository.countByRiskLevel(risk));
        }

        // Fitness Decision Distribution
        Map<String, Long> fitnessDistribution = new HashMap<>();
        for (ClinicalDecision.FitnessDecision decision : ClinicalDecision.FitnessDecision.values()) {
            fitnessDistribution.put(decision.name(), clinicalDecisionRepository.countByFitnessDecision(decision));
        }

        return AnalyticsResponse.builder()
                .totalPatients(totalPatients)
                .patientsThisMonth(patientsThisMonth)
                .reportsGenerated(reportsGenerated)
                .averageAge(averageAge)
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .maleFemaleRatio(maleFemaleRatio)
                .riskDistribution(riskDistribution)
                .fitnessDecisionDistribution(fitnessDistribution)
                .build();
    }
}
