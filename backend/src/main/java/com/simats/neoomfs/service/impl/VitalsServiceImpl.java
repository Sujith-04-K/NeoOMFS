package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.PatientVitalsRequest;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.PatientVitals;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.repository.PatientVitalsRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class VitalsServiceImpl {

    private final PatientVitalsRepository vitalsRepository;
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

    public PatientVitals saveOrUpdate(Long patientId, PatientVitalsRequest request) {
        Patient patient = getPatient(patientId);
        PatientVitals vitals = vitalsRepository.findByPatientId(patientId)
                .orElse(PatientVitals.builder().patient(patient).build());

        vitals.setBpSystolic(request.getBpSystolic());
        vitals.setBpDiastolic(request.getBpDiastolic());
        vitals.setPulseRate(request.getPulseRate());
        
        BigDecimal temp = request.getTemperature();
        if (temp != null) {
            if (temp.compareTo(BigDecimal.valueOf(50.0)) > 0) {
                // Convert Fahrenheit to Celsius: (F - 32) * 5/9
                temp = temp.subtract(BigDecimal.valueOf(32))
                        .multiply(BigDecimal.valueOf(5))
                        .divide(BigDecimal.valueOf(9), 1, RoundingMode.HALF_UP);
            }
            vitals.setTemperature(temp);
        } else {
            vitals.setTemperature(null);
        }

        vitals.setSpo2(request.getSpo2());
        vitals.setRespiratoryRate(request.getRespiratoryRate());
        vitals.setHeightCm(request.getHeightCm());
        vitals.setWeightKg(request.getWeightKg());
        vitals.setRandomBloodSugar(request.getRandomBloodSugar());
        vitals.setNotes(request.getNotes());

        // Calculate BMI automatically
        if (request.getHeightCm() != null && request.getWeightKg() != null
                && request.getHeightCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = request.getHeightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal bmi = request.getWeightKg().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
            vitals.setBmi(bmi);
        }
        PatientVitals saved = vitalsRepository.save(vitals);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patientId, "VITALS", "SAVE", "Saved vitals for patient: " + patient.getMrn(), "PatientVitals", saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public PatientVitals get(Long patientId) {
        return vitalsRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Vitals", "patientId", patientId));
    }

    private Patient getPatient(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}
