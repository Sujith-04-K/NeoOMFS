package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.response.DashboardResponse;
import com.simats.neoomfs.entity.ClinicalDecision;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.repository.AssessmentReportRepository;
import com.simats.neoomfs.repository.ClinicalDecisionRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final ClinicalDecisionRepository clinicalDecisionRepository;
    private final AssessmentReportRepository assessmentReportRepository;

    @Override
    public DashboardResponse getDashboardSummary() {

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Long totalPatients = patientRepository.countByDeletedFalse();

        Long todayPatients = patientRepository.countByCreatedAtAfter(startOfToday);

        Long pendingClinicalDecision = patientRepository.searchPatients(
                null,
                Patient.AssessmentStatus.PENDING,
                null,
                org.springframework.data.domain.PageRequest.of(0, 1)
        ).getTotalElements();

        Long fitPatients = clinicalDecisionRepository.countByFitnessDecision(
                ClinicalDecision.FitnessDecision.FIT);

        Long reviewPatients = clinicalDecisionRepository.countByFitnessDecision(
                ClinicalDecision.FitnessDecision.REVIEW);

        Long unfitPatients = clinicalDecisionRepository.countByFitnessDecision(
                ClinicalDecision.FitnessDecision.CRITICAL);

        Long reportsGenerated = assessmentReportRepository.count();

        Long highRiskPatients =
                clinicalDecisionRepository.countByRiskLevel(
                        ClinicalDecision.RiskLevel.HIGH)
                +
                clinicalDecisionRepository.countByRiskLevel(
                        ClinicalDecision.RiskLevel.VERY_HIGH);

        return DashboardResponse.builder()
                .totalPatients(totalPatients)
                .todayPatients(todayPatients)
                .pendingClinicalDecision(pendingClinicalDecision)
                .fitPatients(fitPatients)
                .reviewPatients(reviewPatients)
                .unfitPatients(unfitPatients)
                .reportsGenerated(reportsGenerated)
                .highRiskPatients(highRiskPatients)
                .build();
    }
}