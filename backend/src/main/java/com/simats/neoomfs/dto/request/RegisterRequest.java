package com.simats.neoomfs.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Inbound DTO for user registration.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Name must be 2–120 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be 3–60 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /** Role name: ROLE_DOCTOR, ROLE_FACULTY, ROLE_STUDENT, ROLE_ADMIN */
    private String role;

    private String licenseNumber;
    private String department;
    private String institution;
    private String phoneNumber;
}
