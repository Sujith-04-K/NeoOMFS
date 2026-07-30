package com.simats.neoomfs.dto.response;

import lombok.*;

import java.util.List;

/**
 * Auth response returned after login or token refresh.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserProfileResponse user;
}
