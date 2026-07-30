package com.simats.neoomfs.models

data class ErrorResponse(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val fieldErrors: Map<String, String>? = null
)
