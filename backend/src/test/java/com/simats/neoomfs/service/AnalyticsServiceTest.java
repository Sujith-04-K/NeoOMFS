package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.AnalyticsResponse;
import com.simats.neoomfs.entity.ClinicalDecision;
import com.simats.neoomfs.repository.AssessmentReportRepository;
import com.simats.neoomfs.repository.ClinicalDecisionRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ClinicalDecisionRepository clinicalDecisionRepository;

    @Mock
    private AssessmentReportRepository assessmentReportRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    public void testGetAnalytics() {
        when(patientRepository.countByDeletedFalse()).thenReturn(100L);
        when(patientRepository.countByCreatedAtAfter(any())).thenReturn(10L);
        when(assessmentReportRepository.count()).thenReturn(50L);
        when(patientRepository.getAverageAge()).thenReturn(35.5);
        when(patientRepository.countByGenderIgnoreCaseAndDeletedFalse("male")).thenReturn(60L);
        when(patientRepository.countByGenderIgnoreCaseAndDeletedFalse("female")).thenReturn(40L);

        when(clinicalDecisionRepository.countByRiskLevel(any(ClinicalDecision.RiskLevel.class))).thenReturn(25L);
        when(clinicalDecisionRepository.countByFitnessDecision(any(ClinicalDecision.FitnessDecision.class))).thenReturn(30L);

        AnalyticsResponse response = analyticsService.getAnalytics();

        assertNotNull(response);
        assertEquals(100, response.getTotalPatients());
        assertEquals(10, response.getPatientsThisMonth());
        assertEquals(50, response.getReportsGenerated());
        assertEquals(35.5, response.getAverageAge());
        assertEquals(60, response.getMaleCount());
        assertEquals(40, response.getFemaleCount());
        assertEquals("1.50", response.getMaleFemaleRatio());
        assertEquals(25L, response.getRiskDistribution().get("LOW"));
        assertEquals(30L, response.getFitnessDecisionDistribution().get("FIT"));
    }
}
