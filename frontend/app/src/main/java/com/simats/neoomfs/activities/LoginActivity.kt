package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
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

            authViewModel.login(email, password)
        }

        // Observe ViewModel state
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> { /* do nothing */ }
                    is AuthState.Loading -> {
                        // Ideally show a progress bar
                        btnSignIn.isEnabled = false
                    }
                    is AuthState.Success -> {
                        btnSignIn.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Welcome ${state.authResponse.user.fullName}!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        btnSignIn.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Login Failed: ${state.message}", Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.PasswordResetSent,
                    is AuthState.SignUpSuccess,
                    is AuthState.PasswordResetComplete -> {
                        btnSignIn.isEnabled = true
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
