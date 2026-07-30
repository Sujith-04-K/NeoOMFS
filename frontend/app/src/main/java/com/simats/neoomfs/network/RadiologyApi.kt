package com.simats.neoomfs.network

import com.simats.neoomfs.models.ApiResponse
import com.simats.neoomfs.models.RadiologyRequest
import com.simats.neoomfs.models.RadiologyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface RadiologyApi {
    @POST("patients/{id}/radiology")
    suspend fun saveRadiology(
        @Path("id") patientId: Long,
        @Body request: RadiologyRequest
    ): Response<ApiResponse<RadiologyResponse>>
}
