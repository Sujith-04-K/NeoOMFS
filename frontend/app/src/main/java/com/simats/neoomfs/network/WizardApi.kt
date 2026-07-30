package com.simats.neoomfs.network

import com.simats.neoomfs.models.ApiResponse
import com.simats.neoomfs.models.AssessmentReportResponse
import com.simats.neoomfs.models.ClinicalDecisionRequest
import com.simats.neoomfs.models.ClinicalDecisionResponse
import com.simats.neoomfs.models.DentalExaminationRequest
import com.simats.neoomfs.models.LaboratoryRequest
import com.simats.neoomfs.models.MedicalHistoryRequest
import com.simats.neoomfs.models.PatientVitalsRequest
import com.simats.neoomfs.models.RadiologyRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WizardApi {
    @POST("patients/{id}/vitals")
    suspend fun saveVitals(@Path("id") patientId: Long, @Body request: PatientVitalsRequest): Response<ApiResponse<Any>>

    @POST("patients/{id}/radiology")
    suspend fun saveRadiology(@Path("id") patientId: Long, @Body request: RadiologyRequest): Response<ApiResponse<Any>>

    @POST("patients/{id}/laboratory")
    suspend fun saveLaboratory(@Path("id") patientId: Long, @Body request: LaboratoryRequest): Response<ApiResponse<Any>>

    @POST("patients/{id}/medical-history")
    suspend fun saveMedicalHistory(@Path("id") patientId: Long, @Body request: MedicalHistoryRequest): Response<ApiResponse<Any>>

    @POST("patients/{id}/dental")
    suspend fun saveDental(@Path("id") patientId: Long, @Body request: DentalExaminationRequest): Response<ApiResponse<Any>>

    @POST("patients/{id}/decision/evaluate")
    suspend fun evaluateDecision(@Path("id") patientId: Long): Response<ApiResponse<ClinicalDecisionResponse>>

    @PUT("patients/{id}/decision/notes")
    suspend fun saveDecisionNotes(@Path("id") patientId: Long, @Body request: ClinicalDecisionRequest): Response<ApiResponse<ClinicalDecisionResponse>>

    @POST("patients/{id}/report/generate")
    suspend fun generateReport(@Path("id") patientId: Long): Response<ApiResponse<AssessmentReportResponse>>
}
