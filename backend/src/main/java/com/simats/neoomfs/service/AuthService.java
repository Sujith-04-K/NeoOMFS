package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.request.*;
import com.simats.neoomfs.dto.response.AuthResponse;
import com.simats.neoomfs.dto.response.UserProfileResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void changePassword(String userEmail, ChangePasswordRequest request);
    UserProfileResponse getProfile(String email);
    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
