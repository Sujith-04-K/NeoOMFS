package com.simats.neoomfs.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.neoomfs.decisionengine.ClinicalDecisionEngine;
import com.simats.neoomfs.decisionengine.DecisionResult;
import com.simats.neoomfs.decisionengine.PatientDataContext;
import com.simats.neoomfs.dto.request.ClinicalDecisionRequest;
import com.simats.neoomfs.dto.response.ClinicalDecisionResponse;
import com.simats.neoomfs.entity.*;
import com.simats.neoomfs.exception.BusinessRuleException;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.mapper.ClinicalDecisionMapper;
import com.simats.neoomfs.repository.*;
import com.simats.neoomfs.service.AuditLogService;
import com.simats.neoomfs.service.ClinicalDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClinicalDecisionServiceImpl implements ClinicalDecisionService {

    private final PatientRepository patientRepository;
    private final PatientVitalsRepository vitalsRepository;
    private final RadiologyRepository radiologyRepository;
    private final LaboratoryInvestigationsRepository labRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationsRepository medicationsRepository;
    private final DentalExaminationRepository dentalRepository;
    private final ClinicalDecisionRepository decisionRepository;
    private final UserRepository userRepository;

    private final ClinicalDecisionEngine decisionEngine;
    private final ClinicalDecisionMapper decisionMapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    @Override
    public ClinicalDecisionResponse evaluateFitness(Long patientId, String doctorEmail) {
        Patient patient = patientRepository.findByIdAndDeletedFalse(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", doctorEmail));

        // Gather all context records
        PatientVitals vitals = vitalsRepository.findByPatientId(patientId).orElse(null);
        Radiology radiology = radiologyRepository.findByPatientId(patientId).orElse(null);
        LaboratoryInvestigations labs = labRepository.findByPatientId(patientId).orElse(null);
        MedicalHistory history = medicalHistoryRepository.findByPatientId(patientId).orElse(null);
        List<Medications> medications = medicationsRepository.findByPatientId(patientId);
        DentalExamination dental = dentalRepository.findByPatientId(patientId).orElse(null);

        // We require vitals, dental, medical history, and labs for complete assessment
        if (vitals == null) {
            throw new BusinessRuleException("Cannot evaluate fitness: Patient Vitals are missing.");
        }
        if (dental == null) {
            throw new BusinessRuleException("Cannot evaluate fitness: Dental Examination is missing.");
        }

        PatientDataContext context = PatientDataContext.builder()
                .patient(patient)
                .vitals(vitals)
                .radiology(radiology)
                .laboratory(labs)
                .medicalHistory(history)
                .medications(medications)
                .dental(dental)
                .build();

        // Run evaluation
        DecisionResult result = decisionEngine.evaluate(context);

        // Create or update decision
        ClinicalDecision decision = decisionRepository.findByPatientId(patientId)
                .orElse(ClinicalDecision.builder().patient(patient).build());

        decision.setRiskLevel(result.getRiskLevel() != null ? ClinicalDecision.RiskLevel.valueOf(result.getRiskLevel().toUpperCase()) : null);
        decision.setFitnessDecision(result.getFitnessDecision() != null ? ClinicalDecision.FitnessDecision.valueOf(result.getFitnessDecision().toUpperCase()) : null);
        decision.setRiskScore(result.getRiskScore());
        decision.setGeneratedBy(doctor);
        decision.setGeneratedAt(LocalDateTime.now());

        try {
            decision.setClinicalAlerts(objectMapper.writeValueAsString(result.getAlerts()));
            decision.setRecommendations(objectMapper.writeValueAsString(result.getRecommendations()));
        } catch (Exception e) {
            log.error("Failed to serialize decision alerts/recommendations to JSON String", e);
            throw new BusinessRuleException("Error storing clinical decision calculations.");
        }

        decision = decisionRepository.save(decision);

        auditLogService.log(doctor.getId(), doctor.getUsername(), patientId, "CLINICAL_DECISION", "GENERATE", "Generated clinical decision for patient: " + patient.getMrn(), "ClinicalDecision", decision.getId());

        return decisionMapper.toResponse(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalDecisionResponse getDecision(Long patientId) {
        ClinicalDecision decision = decisionRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("ClinicalDecision", "patientId", patientId));
        return decisionMapper.toResponse(decision);
    }

    @Override
    public ClinicalDecisionResponse saveCustomNotes(Long patientId, ClinicalDecisionRequest request) {
        ClinicalDecision decision = decisionRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("ClinicalDecision", "patientId", patientId));
        decision.setDecisionNotes(request.getDecisionNotes());
        
        ClinicalDecision saved = decisionRepository.save(decision);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patientId, "CLINICAL_DECISION", "UPDATE_NOTES", "Updated decision notes for patient: " + saved.getPatient().getMrn(), "ClinicalDecision", saved.getId());

        return decisionMapper.toResponse(saved);
    }
}
