package com.simats.neoomfs.network

import com.simats.neoomfs.models.ApiResponse
import com.simats.neoomfs.models.PagedResponse
import com.simats.neoomfs.models.PatientRequest
import com.simats.neoomfs.models.PatientResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PatientApi {
    @POST("patients")
    suspend fun createPatient(@Body request: PatientRequest): Response<ApiResponse<PatientResponse>>

    @GET("patients")
    suspend fun searchPatients(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("doctorId") doctorId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PagedResponse<PatientResponse>>>
}
