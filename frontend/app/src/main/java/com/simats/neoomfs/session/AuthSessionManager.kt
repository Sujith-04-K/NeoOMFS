package com.simats.neoomfs.session

import android.content.Context
import com.google.gson.Gson
import com.simats.neoomfs.models.UserProfileResponse

class AuthSessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("neoomfs_auth", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSession(accessToken: String, refreshToken: String, user: UserProfileResponse) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUser(): UserProfileResponse? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return runCatching { gson.fromJson(json, UserProfileResponse::class.java) }.getOrNull()
    }

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user"
    }
}
