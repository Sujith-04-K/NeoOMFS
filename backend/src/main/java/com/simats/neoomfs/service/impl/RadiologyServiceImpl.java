package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.RadiologyRequest;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.Radiology;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.repository.RadiologyRepository;
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
public class RadiologyServiceImpl {

    private final RadiologyRepository radiologyRepository;
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

    public Radiology saveOrUpdate(Long patientId, RadiologyRequest request) {
        Patient patient = getPatient(patientId);
        Radiology radiology = radiologyRepository.findByPatientId(patientId)
                .orElse(Radiology.builder().patient(patient).build());

        radiology.setIopaTaken(request.isIopaTaken());
        radiology.setIopaFileUrl(request.getIopaFileUrl());
        radiology.setIopaFindings(request.getIopaFindings());
        radiology.setOpgTaken(request.isOpgTaken());
        radiology.setOpgFileUrl(request.getOpgFileUrl());
        radiology.setOpgFindings(request.getOpgFindings());
        radiology.setCbctTaken(request.isCbctTaken());
        radiology.setCbctFileUrl(request.getCbctFileUrl());
        radiology.setCbctFindings(request.getCbctFindings());
        radiology.setBoneDensityHu(request.getBoneDensityHu());
        radiology.setGeneralRadiologyNotes(request.getGeneralRadiologyNotes());

        Radiology saved = radiologyRepository.save(radiology);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patientId, "RADIOLOGY", "SAVE", "Saved radiology findings for patient: " + patient.getMrn(), "Radiology", saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public Radiology get(Long patientId) {
        return radiologyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology", "patientId", patientId));
    }

    private Patient getPatient(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}
