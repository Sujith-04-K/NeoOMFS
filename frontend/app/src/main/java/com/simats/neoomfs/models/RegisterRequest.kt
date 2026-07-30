package com.simats.neoomfs.models

data class RegisterRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "ROLE_DOCTOR",
    val licenseNumber: String? = null,
    val department: String? = "Oral & Maxillofacial Surgery",
    val institution: String? = null,
    val phoneNumber: String? = null
)
