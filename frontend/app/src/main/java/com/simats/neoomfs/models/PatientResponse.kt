package com.simats.neoomfs.models

data class PatientResponse(
    val id: Long,
    val mrn: String?,
    val fullName: String,
    val age: Int?,
    val gender: String?,
    val dateOfBirth: String?,
    val phoneNumber: String?,
    val address: String?,
    val bloodGroup: String?,
    val emergencyContact: String?,
    val emergencyPhone: String?,
    val assessmentStatus: String?,
    val procedureType: String?,
    val referringDoctor: String?,
    val createdByName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val submittedBy: String? = null,
    val reviewedByName: String? = null,
    val reviewComments: String? = null,
    val approvedAt: String? = null
)
