package com.simats.neoomfs.activities

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.R
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        RetrofitClient.initialize(applicationContext)
        authRepository = AuthRepository(applicationContext)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnLogOut = findViewById<LinearLayout>(R.id.btnLogOut)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileRole = findViewById<TextView>(R.id.tvProfileRole)
        val tvLicenseNumber = findViewById<TextView>(R.id.tvLicenseNumber)
        val tvDepartment = findViewById<TextView>(R.id.tvDepartment)
        val tvInstitution = findViewById<TextView>(R.id.tvInstitution)
        val tvSecurityRole = findViewById<TextView>(R.id.tvSecurityRole)
        val tvLoginProtocol = findViewById<TextView>(R.id.tvLoginProtocol)
        val tvSessionDuration = findViewById<TextView>(R.id.tvSessionDuration)
        val tvLastNode = findViewById<TextView>(R.id.tvLastNode)

        // Bottom Navigation views
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<LinearLayout>(R.id.btnNavAssess)
        val btnNavSettings = findViewById<FrameLayout>(R.id.btnNavSettings)

        // Actions Click Triggers
        btnBack.setOnClickListener {
            finish()
        }

        fun populateProfile(profile: com.simats.neoomfs.models.UserProfileResponse) {
            tvProfileName.text = profile.fullName.ifBlank { profile.email }
            tvProfileRole.text = profile.roles.firstOrNull()?.removePrefix("ROLE_") ?: "Doctor"
            tvLicenseNumber.text = if (profile.licenseNumber.isNullOrBlank()) "Not provided" else profile.licenseNumber
            tvDepartment.text = profile.department ?: "Oral & Maxillofacial Surgery"
            tvInstitution.text = profile.institution ?: "Not provided"
            tvSecurityRole.text = "Active • Verified Medical Credential"
            tvLoginProtocol.text = "Secured JWT Auth • ${profile.roles.firstOrNull() ?: "ROLE_DOCTOR"}"
            tvSessionDuration.text = "Active Session • Logged in ${profile.lastLogin ?: "Today"}"
            tvLastNode.text = "User ID #${profile.id} • ${profile.email}"
        }

        authRepository.getStoredUser()?.let { populateProfile(it) }

        lifecycleScope.launch {
            authRepository.getProfile()
                .onSuccess { profile ->
                    populateProfile(profile)
                }
                .onFailure {
                    // Stored user is already displayed
                }
        }

        btnLogOut.setOnClickListener {
            authRepository.logout()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Shifting Bottom Navigation Setup
        btnNavDashboard.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }

        btnNavPatients.setOnClickListener {
            val intent = Intent(this, PatientLogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }

        btnNavAssess.setOnClickListener {
            Toast.makeText(this, "You are already viewing surgeon profile", Toast.LENGTH_SHORT).show()
        }

        btnNavSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }
    }
}
