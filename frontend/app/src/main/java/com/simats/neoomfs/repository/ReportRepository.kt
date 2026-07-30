package com.simats.neoomfs.repository

import com.simats.neoomfs.models.AssessmentReportResponse
import com.simats.neoomfs.network.RetrofitClient

class ReportRepository {
    private val reportApi = RetrofitClient.reportApi

    suspend fun listReports(patientId: Long): Result<List<AssessmentReportResponse>> {
        return try {
            val response = reportApi.listReports(patientId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to load reports"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
