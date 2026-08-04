package com.simats.neoomfs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Name must be 2–120 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be 3–60 characters")
    private String username;

    @Size(max = 50, message = "License number must be at most 50 characters")
    private String licenseNumber;

    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;

    @Size(max = 150, message = "Institution must be at most 150 characters")
    private String institution;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;
}
