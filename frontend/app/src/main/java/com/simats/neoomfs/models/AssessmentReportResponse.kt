package com.simats.neoomfs.models

data class AssessmentReportResponse(
    val id: Long,
    val patientId: Long,
    val patientName: String?,
    val patientMrn: String?,
    val reportFileName: String?,
    val downloadUrl: String?,
    val reportGeneratedAt: String?,
    val reportVersion: Int?,
    val generatedByName: String?
)
