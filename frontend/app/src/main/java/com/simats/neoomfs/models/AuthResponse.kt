package com.simats.neoomfs.models

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val user: UserProfileResponse
)
