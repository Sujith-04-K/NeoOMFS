package com.simats.neoomfs.models

data class PatientVitalsRequest(
    val bpSystolic: Int?,
    val bpDiastolic: Int?,
    val temperature: Double?,
    val pulseRate: Int?,
    val spo2: Double?,
    val respiratoryRate: Int?,
    val heightCm: Double?,
    val weightKg: Double?,
    val bmi: Double?,
    val randomBloodSugar: Double? = null,
    val notes: String? = null
)
