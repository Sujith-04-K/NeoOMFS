package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.util.Patterns
import android.widget.CheckBox
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

class SignUpActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etHospital = findViewById<EditText>(R.id.etHospital)
        val etLicense = findViewById<EditText>(R.id.etLicense)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivEyeToggle = findViewById<ImageView>(R.id.ivEyeToggle)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val tvTermsText = findViewById<TextView>(R.id.tvTermsText)
        val btnSignUp = findViewById<LinearLayout>(R.id.btnSignUp)
        val tvSignInLink = findViewById<TextView>(R.id.tvSignInLink)

        // Make "Terms of Service" look clickable and bold in HTML
        val termsHtml = "I agree to the <font color='#2563EB'><b>Terms of Service</b></font> and acknowledge the clinical data privacy protocols."
        tvTermsText.text = Html.fromHtml(termsHtml, Html.FROM_HTML_MODE_LEGACY)

        // Eye Toggle for Password Visibility
        ivEyeToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivEyeToggle.setImageResource(R.drawable.ic_eye) // using eye icon
                ivEyeToggle.alpha = 0.5f // dim to show active state
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivEyeToggle.setImageResource(R.drawable.ic_eye)
                ivEyeToggle.alpha = 1.0f
            }
            // Move cursor to the end
            etPassword.setSelection(etPassword.text.length)
        }

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val hospital = etHospital.text.toString().trim()
            val license = etLicense.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hospital.isEmpty()) {
                Toast.makeText(this, "Please enter your hospital/institution name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (license.isEmpty()) {
                Toast.makeText(this, "Please enter your medical license #", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your professional email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please agree to the Terms of Service to proceed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.signUp(name, hospital, license, email, password)
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> Unit
                    is AuthState.Loading -> btnSignUp.isEnabled = false
                    is AuthState.SignUpSuccess -> {
                        btnSignUp.isEnabled = true
                        Toast.makeText(this@SignUpActivity, state.message, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    is AuthState.Error -> {
                        btnSignUp.isEnabled = true
                        Toast.makeText(this@SignUpActivity, state.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.Success,
                    is AuthState.PasswordResetSent,
                    is AuthState.PasswordResetComplete -> {
                        btnSignUp.isEnabled = true
                    }
                }
            }
        }

        tvSignInLink.setOnClickListener {
            finish()
        }
    }
}
