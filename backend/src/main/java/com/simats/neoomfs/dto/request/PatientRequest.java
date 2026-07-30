package com.simats.neoomfs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Inbound DTO for patient creation and update (Step 1 of the wizard).
 */
@Data
public class PatientRequest {

    @NotBlank(message = "Patient full name is required")
    @Size(min = 2, max = 120)
    private String fullName;

    @NotNull(message = "Age is required")
    private Integer age;

    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender; // Male, Female, Other

    private String bloodGroup;
    private String phoneNumber;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String procedureType;
    private String referringDoctor;
}
