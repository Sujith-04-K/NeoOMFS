package com.simats.neoomfs.models

data class RadiologyResponse(
    val id: Long?,
    val patientId: Long?,
    val iopaTaken: Boolean,
    val iopaFileUrl: String?,
    val iopaFindings: String?,
    val opgTaken: Boolean,
    val opgFileUrl: String?,
    val opgFindings: String?,
    val cbctTaken: Boolean,
    val cbctFileUrl: String?,
    val cbctFindings: String?,
    val boneDensityHu: Double?,
    val generalRadiologyNotes: String?
)
