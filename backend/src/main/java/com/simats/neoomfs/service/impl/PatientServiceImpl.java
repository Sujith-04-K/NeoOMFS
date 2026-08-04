package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.PatientRequest;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.dto.response.PatientResponse;
import com.simats.neoomfs.dto.response.TimelineEventResponse;
import com.simats.neoomfs.entity.AuditLog;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.AuditLogRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.AuditLogService;
import com.simats.neoomfs.service.PatientService;
import com.simats.neoomfs.util.MrnGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final MrnGenerator mrnGenerator;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    @Override
    public PatientResponse createPatient(PatientRequest request, String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", doctorEmail));

        Patient patient = Patient.builder()
                .mrn(mrnGenerator.generateUniqueMrn())
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .bloodGroup(request.getBloodGroup())
                .emergencyContact(request.getEmergencyContact())
                .emergencyPhone(request.getEmergencyPhone())
                .procedureType(request.getProcedureType())
                .referringDoctor(request.getReferringDoctor())
                .createdBy(doctor)
                .assessmentStatus(Patient.AssessmentStatus.DRAFT)
                .build();

        Patient saved = patientRepository.save(patient);
        auditLogService.log(doctor.getId(), doctor.getUsername(), saved.getId(), "PATIENT", "CREATE", "Created patient record for MRN: " + saved.getMrn(), "Patient", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatient(Long id) {
        return toResponse(findActivePatient(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientByMrn(String mrn) {
        Patient patient = patientRepository.findByMrnAndDeletedFalse(mrn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "MRN", mrn));
        return toResponse(patient);
    }

    @Override
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = findActivePatient(id);
        patient.setFullName(request.getFullName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setEmergencyPhone(request.getEmergencyPhone());
        patient.setProcedureType(request.getProcedureType());
        patient.setReferringDoctor(request.getReferringDoctor());
        Patient saved = patientRepository.save(patient);

        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, saved.getId(), "PATIENT", "UPDATE", "Updated patient record for MRN: " + saved.getMrn(), "Patient", saved.getId());
        return toResponse(saved);
    }

    @Override
    public PatientResponse updateReviewStatus(Long id, com.simats.neoomfs.dto.request.ReviewStatusRequest request, String facultyEmail) {
        Patient patient = findActivePatient(id);

        Patient.AssessmentStatus newStatus;
        try {
            newStatus = Patient.AssessmentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.simats.neoomfs.exception.ResourceNotFoundException("AssessmentStatus", "value", request.getStatus());
        }

        User faculty = userRepository.findByEmail(facultyEmail).orElse(null);

        patient.setAssessmentStatus(newStatus);
        patient.setReviewedBy(faculty);
        patient.setReviewComments(request.getReviewComments());

        if (newStatus == Patient.AssessmentStatus.APPROVED) {
            patient.setApprovedAt(java.time.LocalDateTime.now());
        }

        Patient saved = patientRepository.save(patient);

        String facultyName = faculty != null ? faculty.getFullName() : facultyEmail;
        auditLogService.log(
            faculty != null ? faculty.getId() : null,
            facultyEmail,
            saved.getId(),
            "PATIENT",
            "REVIEW",
            "Assessment status updated to " + newStatus.name() + " by " + facultyName,
            "Patient",
            saved.getId()
        );

        return toResponse(saved);
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = findActivePatient(id);
        patient.setDeleted(true);
        patientRepository.save(patient);
        
        User currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        auditLogService.log(userId, username, patient.getId(), "PATIENT", "DELETE", "Soft deleted patient record for MRN: " + patient.getMrn(), "Patient", patient.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientResponse> searchPatients(String search, String status,
                                                          Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Patient.AssessmentStatus statusEnum = null;
        if (StringUtils.hasText(status)) {
            try {
                statusEnum = Patient.AssessmentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status enum
            }
        }

        Page<Patient> patientPage = patientRepository.searchPatients(search, statusEnum, doctorId, pageable);

        List<PatientResponse> content = patientPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<PatientResponse>builder()
                .content(content)
                .page(patientPage.getNumber())
                .size(patientPage.getSize())
                .totalElements(patientPage.getTotalElements())
                .totalPages(patientPage.getTotalPages())
                .last(patientPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getPatientTimeline(Long patientId) {
        findActivePatient(patientId);

        Page<AuditLog> logs = auditLogRepository.findByPatientId(
                patientId, PageRequest.of(0, 100, Sort.by("timestamp").ascending()));

        return logs.getContent().stream()
                .map(this::toTimelineResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientResponse> advancedSearch(
            String mrn, String name, String phone, String doctor,
            String status, String risk, String gender, Integer age,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Patient.AssessmentStatus statusEnum = null;
        if (StringUtils.hasText(status)) {
            try {
                statusEnum = Patient.AssessmentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status enum
            }
        }

        com.simats.neoomfs.entity.ClinicalDecision.RiskLevel riskEnum = null;
        if (StringUtils.hasText(risk)) {
            try {
                riskEnum = com.simats.neoomfs.entity.ClinicalDecision.RiskLevel.valueOf(risk.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid risk enum
            }
        }

        Page<Patient> patientPage = patientRepository.advancedSearch(
                mrn, name, phone, doctor, statusEnum, riskEnum, gender, age, pageable);

        List<PatientResponse> content = patientPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<PatientResponse>builder()
                .content(content)
                .page(patientPage.getNumber())
                .size(patientPage.getSize())
                .totalElements(patientPage.getTotalElements())
                .totalPages(patientPage.getTotalPages())
                .last(patientPage.isLast())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Patient findActivePatient(Long id) {
        return patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    private PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .mrn(p.getMrn())
                .fullName(p.getFullName())
                .age(p.getAge())
                .gender(p.getGender())
                .dateOfBirth(p.getDateOfBirth())
                .phoneNumber(p.getPhoneNumber())
                .address(p.getAddress())
                .bloodGroup(p.getBloodGroup())
                .emergencyContact(p.getEmergencyContact())
                .emergencyPhone(p.getEmergencyPhone())
                .assessmentStatus(p.getAssessmentStatus() != null ? p.getAssessmentStatus().name() : null)
                .procedureType(p.getProcedureType())
                .referringDoctor(p.getReferringDoctor())
                .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private TimelineEventResponse toTimelineResponse(AuditLog entry) {
        String eventName = mapActionToEventName(entry.getModule(), entry.getAction());
        String performedBy = entry.getUsername();

        if (entry.getUser() != null) {
            performedBy = entry.getUser().getFullName() + " (" + entry.getUsername() + ")";
        }

        return TimelineEventResponse.builder()
                .event(eventName)
                .description(entry.getDescription())
                .performedBy(performedBy)
                .timestamp(entry.getTimestamp())
                .build();
    }

    private String mapActionToEventName(String module, String action) {
        if (action == null) return "Unknown Activity";
        if (module == null) module = "";

        switch (action.toUpperCase()) {
            case "CREATE":
                return "Patient Registered";
            case "UPDATE":
                return "Patient Demographics Updated";
            case "DELETE":
                return "Patient Record Deleted";
            case "SAVE":
                switch (module.toUpperCase()) {
                    case "MEDICAL_HISTORY": return "Medical History Recorded";
                    case "VITALS": return "Clinical Vitals Recorded";
                    case "LABORATORY": return "Laboratory Investigations Added";
                    case "RADIOLOGY": return "Radiology Findings Added";
                    case "DENTAL": return "Dental Examination Added";
                }
                break;
            case "GENERATE":
                if ("CLINICAL_DECISION".equalsIgnoreCase(module)) return "Clinical Decision Support Generated";
                if ("REPORT".equalsIgnoreCase(module)) return "Fitness Assessment Report Generated";
                break;
            case "UPDATE_NOTES":
                return "Clinical Decision Notes Updated";
            case "DOWNLOAD":
                return "Fitness Assessment Report Downloaded";
            case "UPLOAD":
                return "Clinical Document Uploaded";
        }

        // Fallback to old mapping or default action name
        switch (action.toUpperCase()) {
            case "PATIENT_CREATED":
                return "Patient Registered";
            case "PATIENT_UPDATED":
                return "Patient Demographics Updated";
            case "PATIENT_DELETED":
                return "Patient Record Deleted";
            case "MEDICAL_HISTORY_SAVED":
                return "Medical History Recorded";
            case "VITALS_SAVED":
                return "Clinical Vitals Recorded";
            case "LABORATORY_SAVED":
                return "Laboratory Investigations Added";
            case "RADIOLOGY_SAVED":
                return "Radiology Findings Added";
            case "DENTAL_EXAMINATION_SAVED":
                return "Dental Examination Added";
            case "CLINICAL_DECISION_GENERATED":
                return "Clinical Decision Support Generated";
            case "DECISION_NOTES_UPDATED":
                return "Clinical Decision Notes Updated";
            case "REPORT_GENERATED":
                return "Fitness Assessment Report Generated";
            case "REPORT_DOWNLOADED":
                return "Fitness Assessment Report Downloaded";
            case "FILE_UPLOADED":
                return "Clinical Document Uploaded";
            default:
                return action;
        }
    }
}
