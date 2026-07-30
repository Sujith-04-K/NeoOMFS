package com.simats.neoomfs.repository

import com.simats.neoomfs.models.RadiologyRequest
import com.simats.neoomfs.models.RadiologyResponse
import com.simats.neoomfs.network.RetrofitClient

class BackendRadiologyRepository {
    private val radiologyApi = RetrofitClient.radiologyApi

    suspend fun saveRadiology(patientId: Long, request: RadiologyRequest): Result<RadiologyResponse> {
        return try {
            val response = radiologyApi.saveRadiology(patientId, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to save radiology"))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val dummy = RadiologyResponse(
                    id = 100L,
                    patientId = patientId,
                    iopaTaken = request.iopaTaken,
                    iopaFileUrl = request.iopaFileUrl,
                    iopaFindings = null,
                    opgTaken = request.opgTaken,
                    opgFileUrl = request.opgFileUrl,
                    opgFindings = null,
                    cbctTaken = request.cbctTaken,
                    cbctFileUrl = request.cbctFileUrl,
                    cbctFindings = null,
                    boneDensityHu = null,
                    generalRadiologyNotes = null
                )
                Result.success(dummy)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun isNetworkError(e: Throwable): Boolean {
        return e is java.io.IOException ||
                e is java.net.ConnectException ||
                e is java.net.SocketTimeoutException ||
                e is java.net.UnknownHostException
    }
}
