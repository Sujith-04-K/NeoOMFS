package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity – represents a clinical staff member (surgeon, doctor, faculty, student).
 * Linked to roles via a join table. Password is BCrypt encoded.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username")
        })
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, length = 60)
    private String username;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "institution", length = 150)
    private String institution;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_reset_token", length = 200)
    private String passwordResetToken;

    @Column(name = "password_reset_expiry")
    private LocalDateTime passwordResetExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
