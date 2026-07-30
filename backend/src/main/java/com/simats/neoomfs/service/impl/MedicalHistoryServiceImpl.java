package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.MedicalHistoryRequest;
import com.simats.neoomfs.entity.MedicalHistory;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.MedicalHistoryRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalHistoryServiceImpl {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    public MedicalHistory saveOrUpdate(Long patientId, MedicalHistoryRequest request) {
        Patient patient = getPatient(patientId);
        MedicalHistory history = medicalHistoryRepository.findByPatientId(patientId)
                .orElse(MedicalHistory.builder().patient(patient).build());

        history.setHypertension(request.isHypertension());
        history.setDiabetes(request.isDiabetes());
        history.setHeartDisease(request.isHeartDisease());
        history.setKidneyDisease(request.isKidneyDisease());
        history.setLiverDisease(request.isLiverDisease());
        history.setThyroidDisorder(request.isThyroidDisorder());
        history.setAsthma(request.isAsthma());
        history.setEpilepsy(request.isEpilepsy());
        history.setBloodDisorder(request.isBloodDisorder());
        history.setHepatitis(request.isHepatitis());
        history.setHivPositive(request.isHivPositive());
        history.setPregnant(request.isPregnant());
        history.setPregnancyTrimester(request.getPregnancyTrimester());
        history.setOtherConditions(request.getOtherConditions());
        history.setCurrentMedications(request.getCurrentMedications());
        history.setAllergies(request.getAllergies());
        history.setPreviousSurgeries(request.getPreviousSurgeries());
        history.setAnaestheticComplications(request.getAnaestheticComplications());
        history.setFamilyHistory(request.getFamilyHistory());
        history.setSocialHistory(request.getSocialHistory());
        history.setNotes(request.getNotes());

        MedicalHistory saved = medicalHistoryRepository.save(history);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patientId, "MEDICAL_HISTORY", "SAVE", "Saved medical history for patient: " + patient.getMrn(), "MedicalHistory", saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public MedicalHistory get(Long patientId) {
        return medicalHistoryRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalHistory", "patientId", patientId));
    }

    private Patient getPatient(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}
