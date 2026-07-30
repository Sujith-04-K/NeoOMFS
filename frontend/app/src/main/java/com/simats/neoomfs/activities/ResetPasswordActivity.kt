package com.simats.neoomfs.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.R
import com.simats.neoomfs.viewmodel.AuthState
import com.simats.neoomfs.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val btnResetPassword = findViewById<LinearLayout>(R.id.btnResetPassword)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        intent?.getStringExtra("email")?.let { email ->
            etEmail.setText(email)
        }
        etOtp.setText("123456")

        btnBack.setOnClickListener { finish() }
        tvBackToLogin.setOnClickListener { finish() }

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val otp = etOtp.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid registered email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (otp.length != 6 || !otp.all { it.isDigit() }) {
                Toast.makeText(this, "Please enter the 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.resetPassword(email, otp, newPassword)
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> Unit
                    is AuthState.Loading -> btnResetPassword.isEnabled = false
                    is AuthState.PasswordResetComplete -> {
                        btnResetPassword.isEnabled = true
                        Toast.makeText(this@ResetPasswordActivity, state.message, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        btnResetPassword.isEnabled = true
                        Toast.makeText(this@ResetPasswordActivity, state.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.Success,
                    is AuthState.PasswordResetSent,
                    is AuthState.SignUpSuccess -> {
                        btnResetPassword.isEnabled = true
                    }
                }
            }
        }
    }
}
