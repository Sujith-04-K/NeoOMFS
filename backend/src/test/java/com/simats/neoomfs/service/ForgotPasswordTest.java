package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.request.ForgotPasswordRequest;
import com.simats.neoomfs.dto.request.ResetPasswordRequest;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.UnauthorizedException;
import com.simats.neoomfs.repository.RefreshTokenRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.email.PasswordResetEmailService;
import com.simats.neoomfs.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PasswordResetEmailService passwordResetEmailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("doctor@simats.ac.in");
        testUser.setUsername("dr.smith");
        testUser.setFullName("Dr. John Smith");
        testUser.setPassword("oldHashedPassword");
    }

    @Test
    public void testForgotPassword_Success() {
        when(userRepository.findByEmail("doctor@simats.ac.in")).thenReturn(Optional.of(testUser));

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("doctor@simats.ac.in");
        authService.forgotPassword(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getPasswordResetToken(), "Password reset OTP should be generated and set");
        assertEquals(6, savedUser.getPasswordResetToken().length(), "OTP should be 6 digits");
        assertNotNull(savedUser.getPasswordResetExpiry(), "Password reset expiry should be set");
        assertTrue(savedUser.getPasswordResetExpiry().isAfter(LocalDateTime.now()), "OTP expiry should be in the future");

        verify(passwordResetEmailService).sendPasswordResetEmail(
                eq("doctor@simats.ac.in"),
                eq("Dr. John Smith"),
                eq(savedUser.getPasswordResetToken())
        );
        verify(auditLogService).log(anyLong(), anyString(), any(), eq("AUTH"), eq("FORGOT_PASSWORD"), anyString(), eq("User"), anyLong());
    }

    @Test
    public void testForgotPassword_EmailFailure_SavesUserAndDoesNotThrow() {
        when(userRepository.findByEmail("doctor@simats.ac.in")).thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("SMTP error"))
                .when(passwordResetEmailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("doctor@simats.ac.in");

        assertDoesNotThrow(() -> authService.forgotPassword(request));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getPasswordResetToken(), "Password reset OTP should still be generated and set");
        assertEquals(6, savedUser.getPasswordResetToken().length(), "OTP should be 6 digits");
    }

    @Test
    public void testResetPassword_Success() {
        testUser.setPasswordResetToken("123456");
        testUser.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("doctor@simats.ac.in")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewSecurePassword@123")).thenReturn("newHashedPassword");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("doctor@simats.ac.in");
        request.setOtp("123456");
        request.setNewPassword("NewSecurePassword@123");

        authService.resetPassword(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("newHashedPassword", savedUser.getPassword(), "User password should be updated to new hashed password");
        assertNull(savedUser.getPasswordResetToken(), "Reset token should be cleared after reset");
        assertNull(savedUser.getPasswordResetExpiry(), "Reset expiry should be cleared after reset");

        verify(refreshTokenRepository).deleteByUser(savedUser);
        verify(auditLogService).log(anyLong(), anyString(), any(), eq("AUTH"), eq("RESET_PASSWORD"), anyString(), eq("User"), anyLong());
    }

    @Test
    public void testResetPassword_InvalidOtp_ThrowsUnauthorizedException() {
        testUser.setPasswordResetToken("123456");
        testUser.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("doctor@simats.ac.in")).thenReturn(Optional.of(testUser));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("doctor@simats.ac.in");
        request.setOtp("999999");
        request.setNewPassword("NewSecurePassword@123");

        assertThrows(UnauthorizedException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testResetPassword_ExpiredOtp_ThrowsUnauthorizedException() {
        testUser.setPasswordResetToken("123456");
        testUser.setPasswordResetExpiry(LocalDateTime.now().minusMinutes(1)); // Expired

        when(userRepository.findByEmail("doctor@simats.ac.in")).thenReturn(Optional.of(testUser));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("doctor@simats.ac.in");
        request.setOtp("123456");
        request.setNewPassword("NewSecurePassword@123");

        assertThrows(UnauthorizedException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any());
    }
}
