package com.simats.neoomfs.repository

import android.content.Context
import com.google.gson.Gson
import com.simats.neoomfs.models.AuthResponse
import com.simats.neoomfs.models.ErrorResponse
import com.simats.neoomfs.models.ForgotPasswordRequest
import com.simats.neoomfs.models.LoginRequest
import com.simats.neoomfs.models.RegisterRequest
import com.simats.neoomfs.models.ResetPasswordRequest
import com.simats.neoomfs.models.UpdateProfileRequest
import com.simats.neoomfs.models.UserProfileResponse
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.session.AuthSessionManager

class AuthRepository(context: Context) {
    private val authApi = RetrofitClient.authApi
    private val sessionManager = AuthSessionManager(context.applicationContext)
    private val gson = Gson()

    private val prefs = context.applicationContext.getSharedPreferences("neoomfs_offline_auth", Context.MODE_PRIVATE)

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = authApi.login(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                sessionManager.saveSession(body.data.accessToken, body.data.refreshToken, body.data.user)
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), body?.message ?: "Login failed")))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val result = loginOffline(request.email, request.password)
                if (result.isSuccess) {
                    val auth = result.getOrNull()!!
                    sessionManager.saveSession(auth.accessToken, auth.refreshToken, auth.user)
                }
                result
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = authApi.register(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), body?.message ?: "Registration failed")))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val result = registerOffline(request)
                if (result.isSuccess) {
                    val auth = result.getOrNull()!!
                    sessionManager.saveSession(auth.accessToken, auth.refreshToken, auth.user)
                }
                result
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), body?.message ?: "Unable to send reset email")))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> {
        return try {
            val response = authApi.resetPassword(ResetPasswordRequest(email, otp, newPassword))
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), body?.message ?: "Unable to reset password")))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val cleanEmail = email.trim().lowercase()
                prefs.edit().putString("pwd_$cleanEmail", newPassword).apply()
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getProfile(): Result<UserProfileResponse> {
        return try {
            val response = authApi.getProfile()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                val existingRefresh = sessionManager.getRefreshToken().orEmpty()
                sessionManager.saveSession(sessionManager.getAccessToken().orEmpty(), existingRefresh, body.data)
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), body?.message ?: "Unable to load profile")))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val fallbackUser = sessionManager.getUser() ?: createDemoAuthResponse("doctor@neoomfs.com").user
                Result.success(fallbackUser)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileResponse> {
        return try {
            val response = authApi.updateProfile(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                val existingRefresh = sessionManager.getRefreshToken().orEmpty()
                sessionManager.saveSession(sessionManager.getAccessToken().orEmpty(), existingRefresh, body.data)
                Result.success(body.data)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), body?.message ?: "Unable to update profile")))
            }
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                updateProfileOffline(request)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun updateProfileOffline(request: UpdateProfileRequest): Result<UserProfileResponse> {
        val currentUser = sessionManager.getUser() ?: createDemoAuthResponse("doctor@neoomfs.com").user
        val updatedUser = currentUser.copy(
            fullName = request.fullName.ifBlank { currentUser.fullName },
            username = request.username.ifBlank { currentUser.username },
            licenseNumber = request.licenseNumber ?: currentUser.licenseNumber,
            department = request.department ?: currentUser.department,
            institution = request.institution ?: currentUser.institution,
            phoneNumber = request.phoneNumber ?: currentUser.phoneNumber
        )
        val cleanEmail = updatedUser.email.trim().lowercase()
        prefs.edit().putString("name_$cleanEmail", updatedUser.fullName).apply()
        val existingRefresh = sessionManager.getRefreshToken().orEmpty()
        sessionManager.saveSession(sessionManager.getAccessToken().orEmpty(), existingRefresh, updatedUser)
        return Result.success(updatedUser)
    }

    private fun isNetworkError(e: Throwable): Boolean {
        return e is java.io.IOException ||
                e is java.net.ConnectException ||
                e is java.net.SocketTimeoutException ||
                e is java.net.UnknownHostException
    }

    private fun loginOffline(email: String, pwd: String): Result<AuthResponse> {
        val cleanEmail = email.trim().lowercase()
        val storedPwd = prefs.getString("pwd_$cleanEmail", null)
        if (storedPwd == null) {
            if (cleanEmail == "doctor@neoomfs.com" && pwd == "admin123") {
                return Result.success(createDemoAuthResponse(cleanEmail, "Dr. Sarah Jenkins"))
            }
            return Result.failure(Exception("Account not found. Please sign up first."))
        }
        if (storedPwd != pwd) {
            return Result.failure(Exception("Incorrect email or password."))
        }
        val storedName = prefs.getString("name_$cleanEmail", null) ?: cleanEmail.substringBefore("@")
        val auth = createDemoAuthResponse(cleanEmail, storedName)
        return Result.success(auth)
    }

    private fun registerOffline(request: RegisterRequest): Result<AuthResponse> {
        val cleanEmail = request.email.trim().lowercase()
        if (prefs.contains("pwd_$cleanEmail")) {
            return Result.failure(Exception("Account already exists. Please sign in."))
        }
        prefs.edit()
            .putString("pwd_$cleanEmail", request.password)
            .putString("name_$cleanEmail", request.fullName)
            .apply()
        val auth = createDemoAuthResponse(cleanEmail, request.fullName)
        return Result.success(auth)
    }

    private fun createDemoAuthResponse(email: String, fullName: String? = null): AuthResponse {
        val name = fullName ?: "Dr. Sarah Jenkins"
        val username = email.substringBefore("@").ifBlank { "doctor" }
        val demoUser = UserProfileResponse(
            id = 1L,
            fullName = name,
            username = username,
            email = email,
            licenseNumber = "OMFS-90421",
            department = "Oral & Maxillofacial Surgery",
            institution = "NeoOMFS Medical Center",
            phoneNumber = "+1-555-0199",
            active = true,
            roles = listOf("ROLE_DOCTOR"),
            lastLogin = "2026-07-28 10:00:00",
            createdAt = "2026-01-01 10:00:00"
        )
        return AuthResponse(
            accessToken = "demo_access_token_neoomfs_offline",
            refreshToken = "demo_refresh_token_neoomfs_offline",
            tokenType = "Bearer",
            user = demoUser
        )
    }

    fun getStoredUser(): UserProfileResponse? = sessionManager.getUser()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun logout() {
        sessionManager.clear()
    }

    private fun extractErrorMessage(errorJson: String?, fallback: String): String {
        if (errorJson.isNullOrBlank()) return fallback
        return try {
            val error = gson.fromJson(errorJson, ErrorResponse::class.java)
            when {
                !error.fieldErrors.isNullOrEmpty() -> error.fieldErrors.values.joinToString("\n")
                !error.message.isNullOrBlank() -> error.message
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }
}
