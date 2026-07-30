package com.simats.neoomfs.models

data class UserProfileResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val email: String,
    val licenseNumber: String?,
    val department: String?,
    val institution: String?,
    val phoneNumber: String?,
    val active: Boolean,
    val roles: List<String>,
    val lastLogin: String?,
    val createdAt: String?
)
