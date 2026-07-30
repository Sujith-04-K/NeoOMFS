package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.LaboratoryRequest;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.LaboratoryInvestigationsRepository;
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
public class LaboratoryServiceImpl {

    private final LaboratoryInvestigationsRepository labRepository;
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

    public LaboratoryInvestigations saveOrUpdate(Long patientId, LaboratoryRequest request) {
        Patient patient = getPatient(patientId);
        LaboratoryInvestigations lab = labRepository.findByPatientId(patientId)
                .orElse(LaboratoryInvestigations.builder().patient(patient).build());

        lab.setHemoglobin(request.getHemoglobin());
        lab.setTotalWbcCount(request.getTotalWbcCount());
        lab.setPlateletCount(request.getPlateletCount());
        lab.setBleedingTime(request.getBleedingTime());
        lab.setClottingTime(request.getClottingTime());
        lab.setPt(request.getPt());
        lab.setInr(request.getInr());
        lab.setAptt(request.getAptt());
        lab.setFastingBloodSugar(request.getFastingBloodSugar());
        lab.setRandomBloodSugar(request.getRandomBloodSugar());
        lab.setHba1c(request.getHba1c());
        lab.setBloodUrea(request.getBloodUrea());
        lab.setSerumCreatinine(request.getSerumCreatinine());
        lab.setSerumBilirubinTotal(request.getSerumBilirubinTotal());
        lab.setSgot(request.getSgot());
        lab.setSgpt(request.getSgpt());
        lab.setBloodGroup(request.getBloodGroup());
        lab.setRhFactor(request.getRhFactor());
        lab.setHivStatus(request.getHivStatus());
        lab.setHbsagStatus(request.getHbsagStatus());
        lab.setHcvStatus(request.getHcvStatus());
        lab.setLabReportFileUrl(request.getLabReportFileUrl());
        lab.setNotes(request.getNotes());

        LaboratoryInvestigations saved = labRepository.save(lab);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patientId, "LABORATORY", "SAVE", "Saved laboratory investigations for patient: " + patient.getMrn(), "LaboratoryInvestigations", saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public LaboratoryInvestigations get(Long patientId) {
        return labRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory", "patientId", patientId));
    }

    private Patient getPatient(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}
