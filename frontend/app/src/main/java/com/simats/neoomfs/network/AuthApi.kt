package com.simats.neoomfs.network

import com.simats.neoomfs.models.ApiResponse
import com.simats.neoomfs.models.AuthResponse
import com.simats.neoomfs.models.ForgotPasswordRequest
import com.simats.neoomfs.models.LoginRequest
import com.simats.neoomfs.models.RegisterRequest
import com.simats.neoomfs.models.ResetPasswordRequest
import com.simats.neoomfs.models.UpdateProfileRequest
import com.simats.neoomfs.models.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Void>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Void>>

    @GET("auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfileResponse>>

    @PUT("auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfileResponse>>
}
