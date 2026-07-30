package com.simats.neoomfs.models

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)
