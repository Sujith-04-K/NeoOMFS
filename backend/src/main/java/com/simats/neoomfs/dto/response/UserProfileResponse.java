package com.simats.neoomfs.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User profile response DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String licenseNumber;
    private String department;
    private String institution;
    private String phoneNumber;
    private boolean active;
    private List<String> roles;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
