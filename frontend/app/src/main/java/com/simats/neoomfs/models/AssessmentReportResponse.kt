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
    val generatedByName: String?,
    val institution: String? = "SIMATS",
    val department: String? = "Department of Oral & Maxillofacial Surgery",
    val reportId: String? = null,
    val reviewedByName: String? = null,
    val approvalStatus: String? = "APPROVED",
    val approvalSignature: String? = null,
    val reviewComments: String? = null,
    val approvedAt: String? = null,
    val clinicalRecommendation: String? = null
)
