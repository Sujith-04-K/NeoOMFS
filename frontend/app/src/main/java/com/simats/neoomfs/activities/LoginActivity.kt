package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
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

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<LinearLayout>(R.id.btnSignIn)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignUpLink = findViewById<TextView>(R.id.tvSignUpLink)
        val tvBtnSignInText = btnSignIn.getChildAt(0) as? TextView
        // Optional progress bar – added to login layout; gracefully null-safe if not present
        val progressBar = findViewById<ProgressBar?>(R.id.progressBarLogin)

        fun setLoading(loading: Boolean) {
            btnSignIn.isEnabled = !loading
            tvBtnSignInText?.text = if (loading) "Signing in…" else "Sign In"
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            authViewModel.login(email, password)
        }

        // Observe ViewModel state
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> { /* do nothing */ }
                    is AuthState.Loading -> {
                        setLoading(true)
                    }
                    is AuthState.Success -> {
                        setLoading(false)
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome ${state.authResponse.user.fullName}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        setLoading(false)
                        val msg = state.message.lowercase()
                        val friendlyMsg = when {
                            msg.contains("incorrect") || msg.contains("invalid") ||
                            msg.contains("password") || msg.contains("credentials") ||
                            msg.contains("unauthorized") ->
                                "Incorrect password. Please try again."
                            msg.contains("not found") || msg.contains("no account") ||
                            msg.contains("user") ->
                                "No account found with this email. Please sign up."
                            msg.contains("timeout") || msg.contains("connect") ||
                            msg.contains("network") || msg.contains("unable to resolve") ->
                                "Connection failed. Check your internet and try again."
                            else -> state.message
                        }
                        Toast.makeText(this@LoginActivity, friendlyMsg, Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.PasswordResetSent,
                    is AuthState.SignUpSuccess,
                    is AuthState.PasswordResetComplete -> {
                        setLoading(false)
                    }
                }
            }
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        tvSignUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
