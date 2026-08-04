package com.simats.neoomfs.repository

import com.simats.neoomfs.models.PatientRequest
import com.simats.neoomfs.models.PatientResponse
import com.simats.neoomfs.models.ReviewStatusRequest
import com.simats.neoomfs.network.RetrofitClient

class BackendPatientRepository {
    private val patientApi = RetrofitClient.patientApi

    companion object {
        private val inMemoryPatients = mutableListOf(
            PatientResponse(
                id = 1001L,
                mrn = "MRN-4091",
                fullName = "Arthur Pendelton",
                age = 58,
                gender = "Male",
                dateOfBirth = "1968-04-12",
                phoneNumber = "+91-98765-43210",
                address = "442 Cedar Ave, Chennai",
                bloodGroup = "O+",
                emergencyContact = "Martha Pendelton",
                emergencyPhone = "+91-98765-43211",
                assessmentStatus = "APPROVED",
                procedureType = "Surgical Extraction",
                referringDoctor = "Dr. Arun Prakash",
                createdByName = "Dr. Sujith Kumar",
                createdAt = "2026-07-28 10:00:00",
                updatedAt = "2026-07-31 09:30:00",
                submittedBy = "Dr. Sujith Kumar",
                reviewedByName = "Dr. Arun Prakash",
                approvedAt = "2026-07-31 09:30:00"
            ),
            PatientResponse(
                id = 1002L,
                mrn = "MRN-4092",
                fullName = "Brenda Vance",
                age = 44,
                gender = "Female",
                dateOfBirth = "1982-11-03",
                phoneNumber = "+91-99887-76655",
                address = "881 Oak Lane, Chennai",
                bloodGroup = "A+",
                emergencyContact = "Robert Vance",
                emergencyPhone = "+91-99887-76656",
                assessmentStatus = "PENDING_REVIEW",
                procedureType = "Biopsy - Maxilla",
                referringDoctor = "Dr. Sarah Jenkins",
                createdByName = "Dr. Sujith Kumar",
                createdAt = "2026-07-29 14:15:00",
                updatedAt = "2026-07-30 18:20:00",
                submittedBy = "Dr. Sujith Kumar"
            ),
            PatientResponse(
                id = 1003L,
                mrn = "MRN-4093",
                fullName = "Charles Montgomery",
                age = 62,
                gender = "Male",
                dateOfBirth = "1964-02-19",
                phoneNumber = "+91-91234-56789",
                address = "120 Park St, Chennai",
                bloodGroup = "B+",
                emergencyContact = "Anne Montgomery",
                emergencyPhone = "+91-91234-56790",
                assessmentStatus = "DRAFT",
                procedureType = "Implant Placement",
                referringDoctor = "Dr. Sujith Kumar",
                createdByName = "Dr. Sujith Kumar",
                createdAt = "2026-07-30 16:45:00",
                updatedAt = "2026-07-30 16:45:00"
            ),
            PatientResponse(
                id = 1004L,
                mrn = "MRN-4094",
                fullName = "David Miller",
                age = 51,
                gender = "Male",
                dateOfBirth = "1975-08-22",
                phoneNumber = "+91-97654-32109",
                address = "742 Evergreen Terr, Chennai",
                bloodGroup = "AB+",
                emergencyContact = "Lisa Miller",
                emergencyPhone = "+91-97654-32110",
                assessmentStatus = "NEEDS_REVISION",
                procedureType = "Alveoloplasty",
                referringDoctor = "Dr. Arun Prakash",
                createdByName = "Dr. Sujith Kumar",
                createdAt = "2026-07-27 11:00:00",
                updatedAt = "2026-07-30 15:30:00",
                submittedBy = "Dr. Sujith Kumar",
                reviewedByName = "Dr. Arun Prakash",
                reviewComments = "Repeat INR test required before extraction."
            ),
            PatientResponse(
                id = 1005L,
                mrn = "MRN-4095",
                fullName = "Elena Rostova",
                age = 39,
                gender = "Female",
                dateOfBirth = "1987-05-15",
                phoneNumber = "+91-98001-12345",
                address = "319 Birchwood Rd, Chennai",
                bloodGroup = "O-",
                emergencyContact = "Viktor Rostov",
                emergencyPhone = "+91-98001-12346",
                assessmentStatus = "APPROVED",
                procedureType = "Cyst Enucleation",
                referringDoctor = "Dr. Sarah Jenkins",
                createdByName = "Dr. Sujith Kumar",
                createdAt = "2026-07-25 09:20:00",
                updatedAt = "2026-07-30 17:00:00",
                submittedBy = "Dr. Sujith Kumar",
                reviewedByName = "Dr. Sarah Jenkins",
                approvedAt = "2026-07-30 17:00:00"
            )
        )
    }

    /**
     * Update the review status via the backend API.
     * Falls back to in-memory update if the network is unavailable.
     */
    suspend fun updateReviewStatus(
        patientId: Long,
        status: String,
        reviewedByName: String?,
        comments: String?
    ): Result<PatientResponse> {
        return try {
            val request = ReviewStatusRequest(
                status = status,
                reviewComments = comments
            )
            val response = patientApi.updateReviewStatus(patientId, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                // Sync the in-memory list so the UI reflects immediately
                val index = inMemoryPatients.indexOfFirst { it.id == patientId }
                if (index != -1) inMemoryPatients[index] = body.data
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Unable to update review status"))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                updateReviewStatusOffline(patientId, status, reviewedByName, comments)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun updateReviewStatusOffline(
        patientId: Long,
        newStatus: String,
        reviewedByName: String?,
        comments: String?
    ): Result<PatientResponse> {
        val index = inMemoryPatients.indexOfFirst { it.id == patientId }
        if (index != -1) {
            val old = inMemoryPatients[index]
            val updated = old.copy(
                assessmentStatus = newStatus,
                reviewedByName = reviewedByName ?: old.reviewedByName,
                reviewComments = comments ?: old.reviewComments,
                approvedAt = if (newStatus == "APPROVED") "2026-07-31 10:30:00" else old.approvedAt
            )
            inMemoryPatients[index] = updated
            return Result.success(updated)
        }
        return Result.failure(Exception("Patient not found"))
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
                    phoneNumber = request.phoneNumber ?: "+91-00000-00000",
                    address = request.address,
                    bloodGroup = request.bloodGroup ?: "O+",
                    emergencyContact = request.emergencyContact,
                    emergencyPhone = request.emergencyPhone,
                    assessmentStatus = "DRAFT",
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
