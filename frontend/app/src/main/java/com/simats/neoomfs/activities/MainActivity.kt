package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.utils.startActivityNoAnimation
import com.simats.neoomfs.models.PatientResponse
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.AuthRepository
import com.simats.neoomfs.repository.BackendPatientRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val patientRepository = BackendPatientRepository()
    private lateinit var authRepository: AuthRepository
    private var allPatients: List<PatientResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RetrofitClient.initialize(applicationContext)
        authRepository = AuthRepository(applicationContext)

        // Dynamic greeting from session user
        val tvGreeting = findViewById<TextView?>(R.id.tvGreeting)
        val tvDate = findViewById<TextView?>(R.id.tvDate)
        val sessionUser = authRepository.getStoredUser()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        val userName = sessionUser?.fullName?.trim()?.ifBlank { null } ?: "Doctor"
        tvGreeting?.text = "$greeting, $userName"
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val dateSuffix = when (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) % 10) {
            1 -> if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 11) "th" else "st"
            2 -> if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 12) "th" else "nd"
            3 -> if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 13) "th" else "rd"
            else -> "th"
        }
        tvDate?.text = "${dateFormat.format(Calendar.getInstance().time)}$dateSuffix • Clinical Overview"

        // Find Quick Action Buttons
        val btnNewAssessment = findViewById<LinearLayout>(R.id.btnNewAssessment)
        val btnAddPatient = findViewById<LinearLayout>(R.id.btnAddPatient)
        val btnUploadRadiology = findViewById<LinearLayout>(R.id.btnUploadRadiology)
        val btnGenerateReport = findViewById<LinearLayout>(R.id.btnGenerateReport)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnRegistryLink = findViewById<LinearLayout>(R.id.btnRegistryLink)
        val tvPatientsCount = findViewById<TextView>(R.id.tvPatientsCount)
        val tvPendingCount = findViewById<TextView>(R.id.tvPendingCount)
        val tvCompletedCount = findViewById<TextView>(R.id.tvCompletedCount)
        val tvHighRiskCount = findViewById<TextView>(R.id.tvHighRiskCount)
        val tvPriorityAlertsBadge = findViewById<TextView>(R.id.tvPriorityAlertsBadge)
        val layoutPriorityAlertsList = findViewById<LinearLayout>(R.id.layoutPriorityAlertsList)
        val layoutActiveCasesList = findViewById<LinearLayout>(R.id.layoutActiveCasesList)
        val bannerReviewNotification = findViewById<LinearLayout>(R.id.bannerReviewNotification)
        bannerReviewNotification.setOnClickListener {
            val intent = Intent(this, PatientLogActivity::class.java)
            startActivity(intent)
        }

        // Find Shifting Bottom Navigation items
        val btnNavDashboard = findViewById<LinearLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<FrameLayout>(R.id.btnNavAssess)
        val btnNavSettings = findViewById<FrameLayout>(R.id.btnNavSettings)

        val btnSurgeonProfile = findViewById<FrameLayout>(R.id.btnSurgeonProfile)

        // Quick Actions Listeners
        btnNewAssessment.setOnClickListener {
            val intent = Intent(this, PatientRegistrationActivity::class.java)
            startActivity(intent)
        }

        btnAddPatient.setOnClickListener {
            val intent = Intent(this, PatientRegistrationActivity::class.java)
            startActivity(intent)
        }

        btnUploadRadiology.setOnClickListener {
            val intent = Intent(this, PatientRegistrationActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Start a patient assessment to upload radiology in Step 3.", Toast.LENGTH_SHORT).show()
        }

        btnGenerateReport.setOnClickListener {
            val intent = Intent(this, PatientLogActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Open a patient record to generate or download reports.", Toast.LENGTH_SHORT).show()
        }

        // Surgeon Profile Click Trigger
        btnSurgeonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            patientRepository.searchPatients(search = null, status = null, size = 50)
                .onSuccess { patients ->
                    allPatients = patients
                    bindDashboardSummary(
                        patients = patients,
                        tvPatientsCount = tvPatientsCount,
                        tvPendingCount = tvPendingCount,
                        tvCompletedCount = tvCompletedCount,
                        tvHighRiskCount = tvHighRiskCount
                    )
                    renderPriorityAlerts(layoutPriorityAlertsList, tvPriorityAlertsBadge, patients)
                    renderActiveCases(layoutActiveCasesList, patients)
                }
                .onFailure {
                    renderPriorityAlerts(layoutPriorityAlertsList, tvPriorityAlertsBadge, emptyList(), "No priority alerts available")
                    renderActiveCases(layoutActiveCasesList, emptyList(), it.message ?: "Unable to load active cases")
                    Toast.makeText(this@MainActivity, it.message ?: "Unable to load dashboard data", Toast.LENGTH_LONG).show()
                }
        }

        // Registry & Patient details navigations
        btnRegistryLink.setOnClickListener {
            val intent = Intent(this, PatientLogActivity::class.java)
            startActivity(intent)
        }

        etSearch.setOnEditorActionListener { _, _, _ ->
            val query = etSearch.text.toString().trim()
            val filtered = if (query.isBlank()) {
                allPatients
            } else {
                allPatients.filter { patient ->
                    listOf(
                        patient.fullName,
                        patient.mrn.orEmpty(),
                        patient.id.toString(),
                        patient.phoneNumber.orEmpty()
                    ).joinToString(" ").contains(query, ignoreCase = true)
                }
            }
            renderPriorityAlerts(
                container = layoutPriorityAlertsList,
                badgeView = tvPriorityAlertsBadge,
                patients = filtered,
                emptyMessage = if (query.isBlank()) "No priority alerts available" else "No priority alerts found for \"$query\""
            )
            renderActiveCases(
                container = layoutActiveCasesList,
                patients = filtered,
                emptyMessage = if (query.isBlank()) "No active cases available" else "No active cases found for \"$query\""
            )
            true
        }

        // Bottom Navigation items clicks
        btnNavDashboard.setOnClickListener {
            // Already on Dashboard screen
            Toast.makeText(this, "You are already on the Dashboard", Toast.LENGTH_SHORT).show()
        }

        btnNavPatients.setOnClickListener {
            // Navigate to Patients Log activity
            val intent = Intent(this, PatientLogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }

        btnNavAssess.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }

        btnNavSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }
    }

    private fun bindDashboardSummary(
        patients: List<PatientResponse>,
        tvPatientsCount: TextView,
        tvPendingCount: TextView,
        tvCompletedCount: TextView,
        tvHighRiskCount: TextView
    ) {
        tvPatientsCount.text = patients.size.toString()
        tvPendingCount.text = patients.count {
            it.assessmentStatus.equals("PENDING_REVIEW", ignoreCase = true) ||
                it.assessmentStatus.equals("DRAFT", ignoreCase = true)
        }.toString()
        tvCompletedCount.text = patients.count {
            it.assessmentStatus.equals("APPROVED", ignoreCase = true)
        }.toString()
        tvHighRiskCount.text = patients.count {
            it.assessmentStatus.equals("NEEDS_REVISION", ignoreCase = true)
        }.toString()
    }

    private fun renderPriorityAlerts(
        container: LinearLayout,
        badgeView: TextView,
        patients: List<PatientResponse>,
        emptyMessage: String = "No priority alerts available"
    ) {
        container.removeAllViews()

        val alertPatients = patients.filter {
            it.assessmentStatus.equals("NEEDS_REVISION", ignoreCase = true) ||
                it.assessmentStatus.equals("PENDING_REVIEW", ignoreCase = true)
        }.take(3)

        badgeView.text = if (alertPatients.isEmpty()) "0 ALERTS" else "${alertPatients.size} ALERTS"

        if (alertPatients.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = emptyMessage
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 12f
                setPadding(0, 8, 0, 12)
            }
            container.addView(emptyView)
            return
        }

        val density = resources.displayMetrics.density
        alertPatients.forEachIndexed { index, patient ->
            val card = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 0 else (12 * density).toInt()
                }
                orientation = LinearLayout.VERTICAL
                background = getDrawable(
                    if (patient.assessmentStatus.equals("CRITICAL", ignoreCase = true)) {
                        R.drawable.bg_card_red
                    } else {
                        R.drawable.bg_card_yellow
                    }
                )
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, PatientLogActivity::class.java))
                }
            }

            val header = android.widget.RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val nameView = TextView(this).apply {
                id = View.generateViewId()
                text = patient.fullName.ifBlank { "Unnamed patient" }
                setTextColor(getColor(R.color.bg_dark_blue))
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
            }

            val statusView = TextView(this).apply {
                layoutParams = android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_END)
                }
                text = patient.assessmentStatus?.uppercase() ?: "REVIEW"
                setTextColor(getStatusColor(patient.assessmentStatus))
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
            }

            header.addView(nameView)
            header.addView(statusView)

            val body = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (8 * density).toInt()
                }
                text = buildPriorityAlertMessage(patient)
                setTextColor(getColor(R.color.text_primary_dark))
                textSize = 13f
            }

            card.addView(header)
            card.addView(body)
            container.addView(card)
        }
    }

    private fun renderActiveCases(
        container: LinearLayout,
        patients: List<PatientResponse>,
        emptyMessage: String = "No active cases available"
    ) {
        container.removeAllViews()

        if (patients.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = emptyMessage
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 12f
                setPadding(0, 16, 0, 12)
            }
            container.addView(emptyView)
            return
        }

        val density = resources.displayMetrics.density
        patients.take(5).forEachIndexed { index, patient ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                background = getDrawable(android.R.drawable.list_selector_background)
                setPadding(0, (12 * density).toInt(), 0, (12 * density).toInt())
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, PatientLogActivity::class.java))
                }
            }

            val patientCell = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val avatar = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt())
                background = getDrawable(R.drawable.bg_circle_blue)
            }

            val avatarText = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                gravity = android.view.Gravity.CENTER
                text = patient.fullName.firstOrNull()?.uppercase() ?: "?"
                setTextColor(getColor(R.color.accent_blue))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }
            avatar.addView(avatarText)

            val patientName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = (10 * density).toInt()
                }
                text = patient.fullName.ifBlank { "Unnamed patient" }
                setTextColor(getColor(R.color.text_primary_dark))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }
            patientCell.addView(avatar)
            patientCell.addView(patientName)

            val statusCell = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                foregroundGravity = android.view.Gravity.CENTER
            }
            statusCell.addView(View(this).apply {
                layoutParams = FrameLayout.LayoutParams((4 * density).toInt(), (16 * density).toInt(), android.view.Gravity.CENTER)
                setBackgroundColor(getStatusColor(patient.assessmentStatus))
            })

            val riskCell = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                foregroundGravity = android.view.Gravity.CENTER
            }
            riskCell.addView(View(this).apply {
                layoutParams = FrameLayout.LayoutParams((8 * density).toInt(), (8 * density).toInt(), android.view.Gravity.CENTER)
                background = getDrawable(R.drawable.bg_chip_green)
                backgroundTintList = android.content.res.ColorStateList.valueOf(getStatusColor(patient.assessmentStatus))
            })

            val stageCell = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            }
            val stageBadge = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.CENTER)
                setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
                background = getDrawable(R.drawable.bg_chip_blue)
                text = patient.assessmentStatus?.uppercase() ?: "NEW"
                setTextColor(getColor(R.color.accent_blue))
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
            }
            stageCell.addView(stageBadge)

            row.addView(patientCell)
            row.addView(statusCell)
            row.addView(riskCell)
            row.addView(stageCell)
            container.addView(row)

            if (index < patients.take(5).lastIndex) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(getColor(android.R.color.darker_gray))
                    alpha = 0.2f
                })
            }
        }
    }

    private fun buildPriorityAlertMessage(patient: PatientResponse): String {
        val statusText = patient.assessmentStatus?.uppercase() ?: "REVIEW"
        val procedureText = patient.procedureType?.takeIf { it.isNotBlank() } ?: "assessment"
        val doctorText = patient.createdByName?.takeIf { it.isNotBlank() } ?: patient.referringDoctor?.takeIf { it.isNotBlank() }

        return when {
            patient.assessmentStatus.equals("NEEDS_REVISION", ignoreCase = true) -> {
                val prefix = "Case returned for revision (${procedureText.lowercase()})"
                if (doctorText != null) "$prefix. Review revision notes from Dr. $doctorText before resubmitting." else "$prefix. Correct the flagged items and resubmit for faculty review."
            }
            patient.assessmentStatus.equals("PENDING_REVIEW", ignoreCase = true) -> {
                val prefix = "Case pending faculty review"
                if (doctorText != null) "$prefix. Submitted by Dr. $doctorText for supervising faculty sign-off." else "$prefix. Awaiting supervising faculty approval."
            }
            else -> "$statusText case — no action required at this time."
        }
    }

    private fun getStatusColor(status: String?): Int {
        return when {
            status.equals("NEEDS_REVISION", ignoreCase = true) -> getColor(R.color.status_red)
            status.equals("PENDING_REVIEW", ignoreCase = true) || status.equals("DRAFT", ignoreCase = true) -> getColor(R.color.status_orange)
            status.equals("APPROVED", ignoreCase = true) -> getColor(R.color.status_green)
            else -> getColor(R.color.accent_blue)
        }
    }
}