package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.request.*;
import com.simats.neoomfs.dto.response.AuthResponse;
import com.simats.neoomfs.dto.response.UserProfileResponse;
import com.simats.neoomfs.entity.*;
import com.simats.neoomfs.exception.*;
import com.simats.neoomfs.repository.*;
import com.simats.neoomfs.security.JwtTokenProvider;
import com.simats.neoomfs.service.AuditLogService;
import com.simats.neoomfs.service.AuthService;
import com.simats.neoomfs.service.email.PasswordResetEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PasswordResetEmailService passwordResetEmailService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        Role.RoleName roleName = parseRole(request.getRole());
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .licenseNumber(request.getLicenseNumber())
                .department(request.getDepartment())
                .institution(request.getInstitution())
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(role))
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        auditLogService.log(user.getId(), user.getUsername(), null, "AUTH", "REGISTER", "User registered with email: " + user.getEmail(), "User", user.getId());

        String accessToken = jwtTokenProvider.generateTokenFromEmail(user.getEmail());
        String refreshToken = generateAndSaveRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        auditLogService.log(user.getId(), user.getUsername(), null, "AUTH", "LOGIN", "User logged in: " + user.getEmail(), "User", user.getId());

        String accessToken = jwtTokenProvider.generateTokenFromEmail(user.getEmail());
        String refreshToken = generateAndSaveRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token is expired");
        }

        User user = stored.getUser();
        String newAccess = jwtTokenProvider.generateTokenFromEmail(user.getEmail());
        String newRefresh = generateAndSaveRefreshToken(user);

        refreshTokenRepository.delete(stored);

        return buildAuthResponse(user, newAccess, newRefresh);
    }

    @Override
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(token -> {
                    auditLogService.log(token.getUser().getId(), token.getUser().getUsername(), null, "AUTH", "LOGOUT", "User logged out", "User", token.getUser().getId());
                    refreshTokenRepository.delete(token);
                });
    }

    @Override
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
        auditLogService.log(user.getId(), user.getUsername(), null, "AUTH", "CHANGE_PASSWORD", "User changed password", "User", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return toProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.getUsername().equalsIgnoreCase(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        user.setFullName(request.getFullName().trim());
        user.setUsername(request.getUsername().trim());
        user.setLicenseNumber(trimToNull(request.getLicenseNumber()));
        user.setDepartment(trimToNull(request.getDepartment()));
        user.setInstitution(trimToNull(request.getInstitution()));
        user.setPhoneNumber(trimToNull(request.getPhoneNumber()));

        user = userRepository.save(user);
        auditLogService.log(user.getId(), user.getUsername(), null, "AUTH", "UPDATE_PROFILE", "User updated profile", "User", user.getId());
        return toProfileResponse(user);
    }

    @Override
    @Transactional(noRollbackFor = MailDeliveryException.class)
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Always generate a true random 6-digit OTP (never hardcode)
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        // Always log at WARN so OTP is visible in server logs even in dev mode or if email fails
        log.warn("==========================================================");
        log.warn(" Password Reset OTP for user: {} ({})", user.getEmail(), user.getFullName());
        log.warn(" OTP Code: {} (expires in 15 minutes)", otp);
        log.warn("==========================================================");
        user.setPasswordResetToken(otp);
        user.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(15));

        try {
            passwordResetEmailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), otp);
            log.info("Password reset email sent successfully to {}", user.getEmail());
        } catch (Exception ex) {
            log.error("Failed to deliver password reset email to {}: {}. OTP can still be checked in Spring Boot console logs.", user.getEmail(), ex.getMessage());
        }

        userRepository.save(user);
        log.info("Password reset OTP generated and email attempted for user {}", user.getEmail());
        auditLogService.log(user.getId(), user.getUsername(), null, "AUTH", "FORGOT_PASSWORD", "Password reset OTP requested", "User", user.getId());
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(request.getOtp())) {
            throw new UnauthorizedException("Invalid OTP");
        }

        if (user.getPasswordResetExpiry() == null || user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("OTP is expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);

        refreshTokenRepository.deleteByUser(user);

        log.info("Password successfully reset for user {}", user.getEmail());
        auditLogService.log(user.getId(), user.getUsername(), null, "AUTH", "RESET_PASSWORD", "Password reset using OTP", "User", user.getId());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateAndSaveRefreshToken(User user) {
        // Delete existing refresh token immediately
        refreshTokenRepository.findByUser(user).ifPresent(existing -> {
            refreshTokenRepository.delete(existing);
            refreshTokenRepository.flush();
        });

        String tokenStr = jwtTokenProvider.generateTokenFromEmail(
                user.getEmail() + "_refresh_" + System.currentTimeMillis());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        refreshTokenRepository.saveAndFlush(refreshToken);

        return tokenStr;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toProfileResponse(user))
                .build();
    }

    private UserProfileResponse toProfileResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .licenseNumber(user.getLicenseNumber())
                .department(user.getDepartment())
                .institution(user.getInstitution())
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .roles(roleNames)
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Role.RoleName parseRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new BusinessRuleException("Role cannot be empty");
        }

        try {
            String normalizedRole = role.trim().toUpperCase();

            // Accept both "DOCTOR" and "ROLE_DOCTOR"
            if (!normalizedRole.startsWith("ROLE_")) {
                normalizedRole = "ROLE_" + normalizedRole;
            }

            return Role.RoleName.valueOf(normalizedRole);

        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException(
                    "Invalid role: " + role +
                    ". Allowed roles are ROLE_ADMIN, ROLE_DOCTOR, ROLE_FACULTY, ROLE_STUDENT"
            );
        }
    }
}
