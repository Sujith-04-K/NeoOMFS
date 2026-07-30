package com.simats.neoomfs.network

import com.simats.neoomfs.models.ApiResponse
import com.simats.neoomfs.models.AssessmentReportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportApi {
    @GET("patients/{id}/report/list")
    suspend fun listReports(@Path("id") patientId: Long): Response<ApiResponse<List<AssessmentReportResponse>>>

    @GET("patients/{id}/report/download")
    suspend fun downloadReport(
        @Path("id") patientId: Long,
        @Query("reportId") reportId: Long? = null
    ): okhttp3.ResponseBody
}
