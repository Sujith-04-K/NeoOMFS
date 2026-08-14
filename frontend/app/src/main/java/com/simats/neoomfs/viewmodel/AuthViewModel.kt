package com.simats.neoomfs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.neoomfs.models.AuthResponse
import com.simats.neoomfs.models.LoginRequest
import com.simats.neoomfs.models.RegisterRequest
import com.simats.neoomfs.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val authResponse: AuthResponse) : AuthState()
    data class PasswordResetSent(val email: String) : AuthState()
    data class PasswordResetComplete(val message: String) : AuthState()
    data class SignUpSuccess(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application.applicationContext)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(email.trim().lowercase(), password))
            result.onSuccess {
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "An unknown error occurred")
            }
        }
    }

    fun signUp(fullName: String, hospital: String, licenseNumber: String, email: String, password: String, role: String, department: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val username = cleanEmail.substringBefore("@").ifBlank { fullName.replace(" ", "").lowercase() }
            val request = RegisterRequest(
                fullName = fullName,
                username = username,
                email = cleanEmail,
                password = password,
                role = role,
                licenseNumber = licenseNumber,
                institution = hospital,
                department = department
            )
            val result = repository.register(request)
            result.onSuccess {
                _authState.value = AuthState.SignUpSuccess("Account created successfully. Please sign in.")
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unable to create account")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.forgotPassword(email.trim().lowercase())
            result.onSuccess {
                _authState.value = AuthState.PasswordResetSent(email.trim().lowercase())
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unable to send reset email")
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.resetPassword(email.trim().lowercase(), otp.trim(), newPassword)
            result.onSuccess {
                _authState.value = AuthState.PasswordResetComplete("Password reset successful. Please sign in.")
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unable to reset password")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
