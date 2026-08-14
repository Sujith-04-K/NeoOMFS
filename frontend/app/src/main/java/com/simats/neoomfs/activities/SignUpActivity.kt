package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
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
        val spRole = findViewById<Spinner>(R.id.spRole)
        val spDepartment = findViewById<Spinner>(R.id.spDepartment)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivEyeToggle = findViewById<ImageView>(R.id.ivEyeToggle)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val tvTermsText = findViewById<TextView>(R.id.tvTermsText)
        val btnSignUp = findViewById<LinearLayout>(R.id.btnSignUp)
        val tvSignInLink = findViewById<TextView>(R.id.tvSignInLink)
        val tvBtnSignUpText = btnSignUp.getChildAt(0) as? TextView
        val progressBar = findViewById<ProgressBar?>(R.id.progressBarSignUp)

        // Setup Role Spinner
        val rolesList = listOf("Doctor (MDS / OMFS)", "Student (BDS / MDS)")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rolesList)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRole.adapter = roleAdapter

        // Setup Department Spinner
        val deptsList = listOf(
            "Oral and Maxillofacial Surgery",
            "Oral Medicine and Radiology",
            "Orthodontics and Dentofacial Orthopaedics",
            "Periodontology",
            "Prosthodontics",
            "Conservative Dentistry and Endodontics",
            "Pedodontics",
            "Oral Pathology and Microbiology"
        )
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deptsList)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDepartment.adapter = deptAdapter

        // Make "Terms of Service" look clickable and bold in HTML
        val termsHtml = "I agree to the <font color='#2563EB'><b>Terms of Service</b></font> and acknowledge the clinical data privacy protocols."
        tvTermsText.text = Html.fromHtml(termsHtml, Html.FROM_HTML_MODE_LEGACY)

        fun setLoading(loading: Boolean) {
            btnSignUp.isEnabled = !loading
            tvBtnSignUpText?.text = if (loading) "Creating account…" else "Create Account"
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Eye Toggle for Password Visibility
        ivEyeToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivEyeToggle.setImageResource(R.drawable.ic_eye)
                ivEyeToggle.alpha = 0.5f
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivEyeToggle.setImageResource(R.drawable.ic_eye)
                ivEyeToggle.alpha = 1.0f
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val hospital = etHospital.text.toString().trim()
            val license = etLicense.text.toString().trim()
            val rawRole = spRole.selectedItem?.toString() ?: "Doctor (MDS / OMFS)"
            val role = if (rawRole.contains("Student")) "ROLE_STUDENT" else "ROLE_DOCTOR"
            val department = spDepartment.selectedItem?.toString() ?: "Oral and Maxillofacial Surgery"
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

            setLoading(true)
            authViewModel.signUp(name, hospital, license, email, password, role, department)
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Idle -> Unit
                    is AuthState.Loading -> setLoading(true)
                    is AuthState.SignUpSuccess -> {
                        setLoading(false)
                        Toast.makeText(
                            this@SignUpActivity,
                            "Account created successfully! Please sign in.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Navigate back to LoginActivity
                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        setLoading(false)
                        val msg = state.message.lowercase()
                        val friendlyMsg = when {
                            msg.contains("already") || msg.contains("exists") ->
                                "An account with this email already exists. Please sign in."
                            msg.contains("timeout") || msg.contains("connect") || msg.contains("network") ->
                                "Connection failed. Check your internet and try again."
                            else -> state.message
                        }
                        Toast.makeText(this@SignUpActivity, friendlyMsg, Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    is AuthState.Success,
                    is AuthState.PasswordResetSent,
                    is AuthState.PasswordResetComplete -> {
                        setLoading(false)
                    }
                }
            }
        }

        tvSignInLink.setOnClickListener {
            finish()
        }
    }
}
