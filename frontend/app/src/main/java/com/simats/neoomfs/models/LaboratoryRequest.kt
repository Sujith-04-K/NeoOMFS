package com.simats.neoomfs.models

data class LaboratoryRequest(
    val hemoglobin: Double?,
    val totalWbcCount: Int?,
    val plateletCount: Int?,
    val bleedingTime: Double?,
    val clottingTime: Double?,
    val pt: Double?,
    val inr: Double?,
    val aptt: Double? = null,
    val fastingBloodSugar: Double?,
    val randomBloodSugar: Double?,
    val hba1c: Double? = null,
    val bloodUrea: Double? = null,
    val serumCreatinine: Double? = null,
    val serumBilirubinTotal: Double? = null,
    val sgot: Double? = null,
    val sgpt: Double? = null,
    val bloodGroup: String?,
    val rhFactor: String? = null,
    val hivStatus: String? = null,
    val hbsagStatus: String? = null,
    val hcvStatus: String? = null,
    val labReportFileUrl: String? = null,
    val notes: String? = null
)
