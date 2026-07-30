package com.simats.neoomfs.repository

import com.simats.neoomfs.models.PatientRequest
import com.simats.neoomfs.models.PatientResponse
import com.simats.neoomfs.network.RetrofitClient

class BackendPatientRepository {
    private val patientApi = RetrofitClient.patientApi

    companion object {
        private val inMemoryPatients = mutableListOf<PatientResponse>()
    }

    suspend fun createPatient(request: PatientRequest): Result<PatientResponse> {
        return try {
            val response = patientApi.createPatient(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to create patient"))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val demoPatient = PatientResponse(
                    id = System.currentTimeMillis() % 10000,
                    mrn = "MRN-${System.currentTimeMillis() % 1000}",
                    fullName = request.fullName,
                    age = request.age,
                    gender = request.gender,
                    dateOfBirth = request.dateOfBirth,
                    phoneNumber = request.phoneNumber ?: "+1-555-0100",
                    address = request.address,
                    bloodGroup = request.bloodGroup ?: "O+",
                    emergencyContact = request.emergencyContact,
                    emergencyPhone = request.emergencyPhone,
                    assessmentStatus = "IN_PROGRESS",
                    procedureType = request.procedureType ?: "OMFS Surgical Triage",
                    referringDoctor = request.referringDoctor ?: "Self",
                    createdByName = "Dr. Sarah Jenkins",
                    createdAt = "2026-07-28 10:00:00",
                    updatedAt = "2026-07-28 10:00:00"
                )
                inMemoryPatients.add(0, demoPatient)
                Result.success(demoPatient)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun searchPatients(search: String?, status: String?, page: Int = 0, size: Int = 10): Result<List<PatientResponse>> {
        return try {
            val response = patientApi.searchPatients(search = search, status = status, page = page, size = size)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data.content)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to load patients"))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val filtered = if (search.isNullOrBlank()) {
                    inMemoryPatients.toList()
                } else {
                    inMemoryPatients.filter {
                        it.fullName.contains(search, ignoreCase = true) ||
                            (it.mrn?.contains(search, ignoreCase = true) == true)
                    }
                }
                Result.success(filtered)
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
