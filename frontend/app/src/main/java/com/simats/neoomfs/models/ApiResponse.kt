package com.simats.neoomfs.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
