package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.request.PatientRequest;
import com.simats.neoomfs.dto.response.PatientResponse;
import com.simats.neoomfs.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public Patient toEntity(PatientRequest request) {
        if (request == null) return null;
        return Patient.builder()
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
                .build();
    }

    public PatientResponse toResponse(Patient patient) {
        if (patient == null) return null;
        return PatientResponse.builder()
                .id(patient.getId())
                .mrn(patient.getMrn())
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth())
                .phoneNumber(patient.getPhoneNumber())
                .address(patient.getAddress())
                .bloodGroup(patient.getBloodGroup())
                .emergencyContact(patient.getEmergencyContact())
                .emergencyPhone(patient.getEmergencyPhone())
                .assessmentStatus(patient.getAssessmentStatus() != null ? patient.getAssessmentStatus().name() : null)
                .procedureType(patient.getProcedureType())
                .referringDoctor(patient.getReferringDoctor())
                .createdByName(patient.getCreatedBy() != null ? patient.getCreatedBy().getFullName() : null)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .submittedBy(patient.getSubmittedBy())
                .reviewedByName(patient.getReviewedBy() != null ? patient.getReviewedBy().getFullName() : null)
                .reviewComments(patient.getReviewComments())
                .approvedAt(patient.getApprovedAt())
                .build();
    }
}
