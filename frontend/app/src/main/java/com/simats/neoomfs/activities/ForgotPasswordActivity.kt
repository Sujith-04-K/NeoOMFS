package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
        val tvBtnText = btnSendResetLink.getChildAt(0) as? TextView
        val progressBar = findViewById<ProgressBar?>(R.id.progressBarForgot)

        fun setLoading(loading: Boolean) {
            btnSendResetLink.isEnabled = !loading
            tvBtnText?.text = if (loading) "Sending…" else "Send Reset OTP"
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        }

        btnBack.setOnClickListener { finish() }
        tvReturnToLogin.setOnClickListener { finish() }

        btnSendResetLink.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your registered email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            authViewModel.sendPasswordReset(email)
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> Unit
                    is AuthState.Loading -> setLoading(true)
                    is AuthState.PasswordResetSent -> {
                        setLoading(false)
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "OTP sent to ${state.email}. Check your inbox (and spam folder).",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(
                            android.content.Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                                .putExtra("email", state.email)
                        )
                        finish()
                    }
                    is AuthState.Error -> {
                        setLoading(false)
                        val msg = state.message.lowercase()
                        val friendlyMsg = when {
                            msg.contains("not found") || msg.contains("no account") || msg.contains("user") ->
                                "No account found with this email address."
                            msg.contains("timeout") || msg.contains("connect") || msg.contains("network") ||
                            msg.contains("unable to resolve") || msg.contains("failed to connect") ->
                                "Connection failed. Please check your internet and try again."
                            else -> state.message
                        }
                        Toast.makeText(this@ForgotPasswordActivity, friendlyMsg, Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.Success,
                    is AuthState.SignUpSuccess,
                    is AuthState.PasswordResetComplete -> {
                        setLoading(false)
                    }
                }
            }
        }
    }
}
