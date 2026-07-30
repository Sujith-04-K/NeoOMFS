package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.DentalExaminationRequest;
import com.simats.neoomfs.entity.DentalExamination;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.DentalExaminationRepository;
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
public class DentalExaminationServiceImpl {

    private final DentalExaminationRepository dentalExaminationRepository;
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

    public DentalExamination saveOrUpdate(Long patientId, DentalExaminationRequest request) {
        Patient patient = getPatient(patientId);
        DentalExamination dental = dentalExaminationRepository.findByPatientId(patientId)
                .orElse(DentalExamination.builder().patient(patient).build());

        dental.setPellGregoryClass(request.getPellGregoryClass());
        dental.setWinterClassification(request.getWinterClassification());
        dental.setUpperThirdMolar(request.getUpperThirdMolar());
        dental.setDifficultyScore(request.getDifficultyScore());
        dental.setAsaClass(request.getAsaClass());
        dental.setPeriodontalStatus(request.getPeriodontalStatus());
        dental.setOralHygieneStatus(request.getOralHygieneStatus());
        dental.setMouthOpeningMm(request.getMouthOpeningMm());
        dental.setToothNumber(request.getToothNumber());
        dental.setClinicalExaminationNotes(request.getClinicalExaminationNotes());
        
        dental.setActiveInfection(request.isActiveInfection());
        dental.setSwelling(request.isSwelling());
        dental.setTrismus(request.isTrismus());

        DentalExamination saved = dentalExaminationRepository.save(dental);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patientId, "DENTAL", "SAVE", "Saved dental examination for patient: " + patient.getMrn(), "DentalExamination", saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public DentalExamination get(Long patientId) {
        return dentalExaminationRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("DentalExamination", "patientId", patientId));
    }

    private Patient getPatient(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}
