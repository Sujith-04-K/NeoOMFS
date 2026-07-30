package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity – maps to the roles table.
 * Uses a string-based role name from the RoleName enum.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private RoleName name;

    /**
     * Supported role names for the NeoOMFS system.
     */
    public enum RoleName {
        ROLE_ADMIN,
        ROLE_DOCTOR,
        ROLE_FACULTY,
        ROLE_STUDENT
    }
}
