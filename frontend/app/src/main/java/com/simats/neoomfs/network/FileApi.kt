package com.simats.neoomfs.network

import com.simats.neoomfs.models.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileApi {
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part("folder") folder: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<String>>
}
