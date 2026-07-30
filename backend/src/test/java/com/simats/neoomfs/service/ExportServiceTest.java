package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.AnalyticsResponse;
import com.simats.neoomfs.entity.AssessmentReport;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.repository.AssessmentReportRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.impl.ExportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExportServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AssessmentReportRepository reportRepository;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private ExportServiceImpl exportService;

    @Test
    public void testExportPatientsCsv() {
        Patient patient = Patient.builder()
                .fullName("John Doe")
                .mrn("MRN101")
                .age(40)
                .gender("Male")
                .bloodGroup("O+")
                .phoneNumber("1234567890")
                .referringDoctor("Dr. Smith")
                .assessmentStatus(Patient.AssessmentStatus.FIT)
                .build();
        patient.setId(1L);

        when(patientRepository.findByDeletedFalse()).thenReturn(Collections.singletonList(patient));

        byte[] csvBytes = exportService.exportPatientsCsv();
        assertNotNull(csvBytes);
        String csv = new String(csvBytes);
        assertTrue(csv.contains("MRN101"));
        assertTrue(csv.contains("John Doe"));
    }

    @Test
    public void testExportPatientsExcel() throws IOException {
        Patient patient = Patient.builder()
                .fullName("John Doe")
                .mrn("MRN101")
                .age(40)
                .gender("Male")
                .build();
        patient.setId(1L);

        when(patientRepository.findByDeletedFalse()).thenReturn(Collections.singletonList(patient));

        byte[] xlsxBytes = exportService.exportPatientsExcel();
        assertNotNull(xlsxBytes);
        assertTrue(xlsxBytes.length > 0);
    }

    @Test
    public void testExportPatientsPdf() {
        Patient patient = Patient.builder()
                .fullName("John Doe")
                .mrn("MRN101")
                .age(40)
                .gender("Male")
                .build();
        patient.setId(1L);

        when(patientRepository.findByDeletedFalse()).thenReturn(Collections.singletonList(patient));

        byte[] pdfBytes = exportService.exportPatientsPdf();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    public void testExportReportsCsv() {
        Patient patient = Patient.builder().fullName("Jane Doe").mrn("MRN102").build();
        AssessmentReport report = AssessmentReport.builder()
                .patient(patient)
                .reportFileName("report_102.pdf")
                .reportVersion(1)
                .reportGeneratedAt(LocalDateTime.now())
                .build();
        report.setId(2L);

        when(reportRepository.findAll()).thenReturn(Collections.singletonList(report));

        byte[] csvBytes = exportService.exportReportsCsv();
        assertNotNull(csvBytes);
        String csv = new String(csvBytes);
        assertTrue(csv.contains("report_102.pdf"));
        assertTrue(csv.contains("MRN102"));
    }

    @Test
    public void testExportAnalyticsCsv() {
        AnalyticsResponse analytics = AnalyticsResponse.builder()
                .totalPatients(10L)
                .patientsThisMonth(2L)
                .reportsGenerated(5L)
                .averageAge(42.0)
                .maleCount(5L)
                .femaleCount(5L)
                .maleFemaleRatio("1.00")
                .riskDistribution(new HashMap<>())
                .fitnessDecisionDistribution(new HashMap<>())
                .build();

        when(analyticsService.getAnalytics()).thenReturn(analytics);

        byte[] csvBytes = exportService.exportAnalyticsCsv();
        assertNotNull(csvBytes);
        String csv = new String(csvBytes);
        assertTrue(csv.contains("Total Patients,10"));
        assertTrue(csv.contains("Average Age,42.0"));
    }
}
