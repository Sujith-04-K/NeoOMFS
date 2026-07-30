package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.viewmodel.AuthState
import com.simats.neoomfs.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSendResetLink = findViewById<LinearLayout>(R.id.btnSendResetLink)
        val tvReturnToLogin = findViewById<TextView>(R.id.tvReturnToLogin)

        btnBack.setOnClickListener {
            finish()
        }

        tvReturnToLogin.setOnClickListener {
            finish()
        }

        btnSendResetLink.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your registered email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.sendPasswordReset(email)
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> Unit
                    is AuthState.Loading -> btnSendResetLink.isEnabled = false
                    is AuthState.PasswordResetSent -> {
                        btnSendResetLink.isEnabled = true
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Demo OTP 123456 sent to ${state.email}. Use 123456 to reset password.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(android.content.Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java).putExtra("email", state.email))
                        finish()
                    }
                    is AuthState.Error -> {
                        btnSendResetLink.isEnabled = true
                        Toast.makeText(this@ForgotPasswordActivity, state.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.Success,
                    is AuthState.SignUpSuccess,
                    is AuthState.PasswordResetComplete -> {
                        btnSendResetLink.isEnabled = true
                    }
                }
            }
        }
    }
}
