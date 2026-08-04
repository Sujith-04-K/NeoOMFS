package com.simats.neoomfs.models

data class UpdateProfileRequest(
    val fullName: String,
    val username: String,
    val licenseNumber: String?,
    val department: String?,
    val institution: String?,
    val phoneNumber: String?
)
