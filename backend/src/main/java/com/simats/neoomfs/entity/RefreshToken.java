package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * RefreshToken entity – persists refresh tokens for stateless JWT auth.
 * Linked one-to-one with a User. Expiry is checked during token refresh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
}
