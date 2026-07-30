package com.simats.neoomfs.models

data class PatientRequest(
    val fullName: String,
    val age: Int,
    val dateOfBirth: String? = null,
    val gender: String,
    val bloodGroup: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val procedureType: String? = null,
    val referringDoctor: String? = null
)
