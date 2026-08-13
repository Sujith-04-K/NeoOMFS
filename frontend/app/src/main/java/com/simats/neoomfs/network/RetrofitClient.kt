package com.simats.neoomfs.network

import android.content.Context
import com.simats.neoomfs.BuildConfig
import com.simats.neoomfs.session.AuthSessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/api/v1/"
    val baseUrl: String
        get() = BuildConfig.API_BASE_URL.ifBlank { DEFAULT_BASE_URL }

    fun resolveApiUrl(relativePath: String): String {
        if (relativePath.startsWith("http://", ignoreCase = true) || relativePath.startsWith("https://", ignoreCase = true)) {
            return relativePath
        }
        val normalizedBase = baseUrl.removeSuffix("/")
        val normalizedPath = if (relativePath.startsWith("/")) relativePath else "/$relativePath"
        return "$normalizedBase$normalizedPath"
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    private var authSessionManager: AuthSessionManager? = null

    fun initialize(context: Context) {
        if (authSessionManager == null) {
            authSessionManager = AuthSessionManager(context.applicationContext)
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = authSessionManager?.getAccessToken()
                val request = if (!token.isNullOrBlank()) {
                    original.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val patientApi: PatientApi by lazy { retrofit.create(PatientApi::class.java) }
    val radiologyApi: RadiologyApi by lazy { retrofit.create(RadiologyApi::class.java) }
    val fileApi: FileApi by lazy { retrofit.create(FileApi::class.java) }
    val wizardApi: WizardApi by lazy { retrofit.create(WizardApi::class.java) }
    val reportApi: ReportApi by lazy { retrofit.create(ReportApi::class.java) }
}
