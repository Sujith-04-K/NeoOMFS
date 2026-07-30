package com.simats.neoomfs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String mrn;
    private String fullName;
    private Integer age;
    private String gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private String bloodGroup;
    private String emergencyContact;
    private String emergencyPhone;
    private String assessmentStatus;
    private String procedureType;
    private String referringDoctor;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
