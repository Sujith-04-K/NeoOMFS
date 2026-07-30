package com.simats.neoomfs.models

data class RadiologyRequest(
    val iopaTaken: Boolean,
    val iopaFileUrl: String?,
    val iopaFindings: String? = null,
    val opgTaken: Boolean,
    val opgFileUrl: String?,
    val opgFindings: String? = null,
    val cbctTaken: Boolean,
    val cbctFileUrl: String?,
    val cbctFindings: String? = null,
    val boneDensityHu: Double? = null,
    val generalRadiologyNotes: String? = null
)
