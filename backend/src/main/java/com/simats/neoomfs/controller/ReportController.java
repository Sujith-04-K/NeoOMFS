package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.AssessmentReportResponse;
import com.simats.neoomfs.entity.*;
import com.simats.neoomfs.exception.BusinessRuleException;
import com.simats.neoomfs.exception.ForbiddenException;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.pdf.ReportGeneratorService;
import com.simats.neoomfs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patients/{id}/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final PatientRepository patientRepository;
    private final PatientVitalsRepository vitalsRepository;
    private final RadiologyRepository radiologyRepository;
    private final LaboratoryInvestigationsRepository labRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final DentalExaminationRepository dentalRepository;
    private final ClinicalDecisionRepository decisionRepository;
    private final AssessmentReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportGeneratorService reportGeneratorService;
    private final com.simats.neoomfs.service.AuditLogService auditLogService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<AssessmentReportResponse>> generateReport(
            @PathVariable("id") Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient patient = patientRepository.findByIdAndDeletedFalse(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        User doctor = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        PatientVitals vitals = vitalsRepository.findByPatientId(patientId).orElse(null);
        Radiology radiology = radiologyRepository.findByPatientId(patientId).orElse(null);
        LaboratoryInvestigations labs = labRepository.findByPatientId(patientId).orElse(null);
        MedicalHistory history = medicalHistoryRepository.findByPatientId(patientId).orElse(null);
        DentalExamination dental = dentalRepository.findByPatientId(patientId).orElse(null);

        ClinicalDecision decision = decisionRepository.findByPatientId(patientId)
                .orElseThrow(() ->
                        new BusinessRuleException("Cannot generate report: Run clinical evaluation first."));

        byte[] pdf = reportGeneratorService.generateReportPdf(
                patient,
                vitals,
                radiology,
                labs,
                history,
                dental,
                decision
        );

        String filePath = reportGeneratorService.saveReportToDisk(pdf, patient.getMrn());

        File file = new File(filePath);

        List<AssessmentReport> reports =
                reportRepository.findByPatientIdOrderByReportGeneratedAtDesc(patientId);

        int version = reports.isEmpty()
                ? 1
                : reports.get(0).getReportVersion() + 1;

        AssessmentReport report = AssessmentReport.builder()
                .patient(patient)
                .reportFilePath(filePath)
                .reportFileName(file.getName())
                .reportGeneratedAt(LocalDateTime.now())
                .generatedBy(doctor)
                .reportVersion(version)
                .build();

        report = reportRepository.save(report);

        auditLogService.log(
                doctor.getId(),
                doctor.getUsername(),
                patient.getId(),
                "REPORT",
                "GENERATE",
                "Generated report version " + report.getReportVersion() + " for patient: " + patient.getMrn(),
                "AssessmentReport",
                report.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Preoperative fitness report generated successfully.",
                        toResponse(report)
                )
        );
    }

    @GetMapping("/download")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<Resource> downloadReport(
            @PathVariable("id") Long patientId,
            @RequestParam(required = false) Long reportId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User doctor = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        Patient patient = patientRepository.findByIdAndDeletedFalse(patientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient", "id", patientId));

        boolean admin =
                doctor.getRoles().stream().anyMatch(r ->
                        r.getName().name().equals("ROLE_ADMIN") ||
                                r.getName().name().equals("ROLE_FACULTY"));

        if (!admin &&
                !patient.getCreatedBy().getId().equals(doctor.getId())) {
            throw new ForbiddenException(
                    "Access denied: You are not authorized to view this patient's reports."
            );
        }

        AssessmentReport report;

        if (reportId != null) {
            report = reportRepository.findById(reportId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "AssessmentReport",
                                    "id",
                                    reportId
                            ));
        } else {

            List<AssessmentReport> reports =
                    reportRepository.findByPatientIdOrderByReportGeneratedAtDesc(patientId);

            if (reports.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No assessment reports found for patient ID: " + patientId
                );
            }

            report = reports.get(0);
        }

        try {

            File file = new File(report.getReportFilePath());

            if (!file.exists()) {
                throw new ResourceNotFoundException("Report PDF not found.");
            }

            Resource resource = new UrlResource(file.toURI());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + report.getReportFileName() + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            log.error("Error downloading report", e);

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<List<AssessmentReportResponse>>> listReports(
            @PathVariable("id") Long patientId) {

        List<AssessmentReportResponse> reports =
                reportRepository
                        .findByPatientIdOrderByReportGeneratedAtDesc(patientId)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    private AssessmentReportResponse toResponse(AssessmentReport report) {

        return AssessmentReportResponse.builder()
                .id(report.getId())
                .patientId(report.getPatient().getId())
                .patientName(report.getPatient().getFullName())
                .patientMrn(report.getPatient().getMrn())
                .reportFileName(report.getReportFileName())
                .downloadUrl("/patients/" +
                        report.getPatient().getId() +
                        "/report/download?reportId=" +
                        report.getId())
                .reportGeneratedAt(report.getReportGeneratedAt())
                .reportVersion(report.getReportVersion())
                .generatedByName(
                        report.getGeneratedBy() == null
                                ? "SYSTEM"
                                : report.getGeneratedBy().getFullName())
                .build();
    }
}