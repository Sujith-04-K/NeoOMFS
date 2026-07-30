package com.simats.neoomfs.models

data class ClinicalDecisionResponse(
    val id: Long?,
    val patientId: Long?,
    val fitnessStatus: String? = null,
    val riskLevel: String? = null,
    val decisionNotes: String? = null,
    val redAlerts: List<String>? = null,
    val yellowAlerts: List<String>? = null,
    val recommendations: List<String>? = null
)
