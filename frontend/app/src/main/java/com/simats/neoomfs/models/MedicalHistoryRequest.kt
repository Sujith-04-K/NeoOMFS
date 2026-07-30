package com.simats.neoomfs.models

data class MedicalHistoryRequest(
    val hypertension: Boolean,
    val diabetes: Boolean,
    val heartDisease: Boolean,
    val kidneyDisease: Boolean,
    val liverDisease: Boolean,
    val thyroidDisorder: Boolean = false,
    val asthma: Boolean,
    val epilepsy: Boolean = false,
    val bloodDisorder: Boolean,
    val hepatitis: Boolean = false,
    val hivPositive: Boolean = false,
    val pregnant: Boolean,
    val pregnancyTrimester: String? = null,
    val otherConditions: String? = null,
    val currentMedications: String? = null,
    val allergies: String? = null,
    val previousSurgeries: String? = null,
    val anaestheticComplications: String? = null,
    val familyHistory: String? = null,
    val socialHistory: String? = null,
    val notes: String? = null
)
