package com.simats.neoomfs.models

data class DentalExaminationRequest(
    val asaClass: String?,
    val pellGregoryClass: String?,
    val winterClassification: String?,
    val upperThirdMolar: String?,
    val difficultyScore: Int?,
    val mouthOpeningMm: Int?,
    val oralHygieneStatus: String? = null,
    val periodontalStatus: String? = null,
    val activeInfection: Boolean,
    val swelling: Boolean,
    val trismus: Boolean,
    val toothNumber: String?,
    val clinicalExaminationNotes: String? = null
)
