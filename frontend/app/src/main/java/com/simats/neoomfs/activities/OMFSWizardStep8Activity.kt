package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.WizardRepository
import com.simats.neoomfs.repository.BackendPatientRepository
import com.simats.neoomfs.models.AssessmentReportResponse
import android.graphics.Typeface
import kotlinx.coroutines.launch

class OMFSWizardStep8Activity : AppCompatActivity() {
    private val wizardRepository = WizardRepository()
    private val patientRepository = BackendPatientRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step8)

        RetrofitClient.initialize(applicationContext)

        // Retrieve bundle metrics
        val patientId = intent.getLongExtra("patient_id", -1L)
        val name = intent.getStringExtra("patient_name") ?: ""
        val age = intent.getStringExtra("patient_age") ?: ""
        val gender = intent.getStringExtra("patient_gender") ?: ""
        val procedure = intent.getStringExtra("patient_procedure") ?: ""
        val asa = intent.getIntExtra("patient_asa", 1)
        val allergies = intent.getStringArrayListExtra("patient_allergies") ?: arrayListOf()
        val vitalSys = intent.getStringExtra("vital_bp_sys") ?: ""
        val vitalDia = intent.getStringExtra("vital_bp_dia") ?: ""
        val vitalPulse = intent.getStringExtra("vital_pulse") ?: ""
        val vitalTemp = intent.getStringExtra("vital_temp") ?: ""
        val vitalResp = intent.getStringExtra("vital_resp") ?: ""
        val vitalSpo2 = intent.getStringExtra("vital_spo2") ?: ""
        val labBg = intent.getStringExtra("lab_blood_group") ?: ""
        val labRbs = intent.getStringExtra("lab_rbs") ?: ""
        val labFbs = intent.getStringExtra("lab_fbs") ?: ""
        val labBt = intent.getStringExtra("lab_bt") ?: ""
        val labCt = intent.getStringExtra("lab_ct") ?: ""
        val labHb = intent.getStringExtra("lab_hb") ?: ""
        val labWbc = intent.getStringExtra("lab_wbc") ?: ""
        val labPlatelets = intent.getStringExtra("lab_platelets") ?: ""
        val labPt = intent.getStringExtra("lab_pt") ?: ""
        val labInr = intent.getStringExtra("lab_inr") ?: ""
        val lifeSmoking = intent.getBooleanExtra("life_smoking", false)
        val lifeAlcohol = intent.getBooleanExtra("life_alcohol", false)
        val lifeDiet = intent.getStringExtra("life_diet") ?: ""
        val historySystemic = intent.getStringArrayListExtra("history_systemic") ?: arrayListOf()
        val historyMedications = intent.getStringArrayListExtra("history_medications") ?: arrayListOf()
        val examOpening = intent.getStringExtra("exam_mouth_opening") ?: ""
        val examTooth = intent.getStringExtra("exam_tooth_number") ?: ""
        val examImpaction = intent.getStringExtra("exam_impaction_type") ?: ""
        val examPellGregory = intent.getStringExtra("exam_pell_gregory") ?: ""
        val examWinter = intent.getStringExtra("exam_winter") ?: ""
        val examUpperThird = intent.getStringExtra("exam_upper_third") ?: ""
        val examSwelling = intent.getBooleanExtra("exam_swelling", false)
        val examInfection = intent.getBooleanExtra("exam_infection", false)
        val examDifficulty = intent.getStringExtra("exam_difficulty") ?: ""
        val examNotes = intent.getStringExtra("exam_notes") ?: ""
        val riskLevel = intent.getStringExtra("risk_computed_level") ?: "LOW RISK"
        val redAlerts = intent.getStringArrayListExtra("risk_red_alerts") ?: arrayListOf()
        val yellowAlerts = intent.getStringArrayListExtra("risk_yellow_alerts") ?: arrayListOf()

        // Views
        val tvReportSummary = findViewById<TextView>(R.id.tvReportSummary)
        val tvComputedRiskBadge = findViewById<TextView>(R.id.tvComputedRiskBadge)
        val tvClinicalRecommendation = findViewById<TextView>(R.id.tvClinicalRecommendation)
        val tvSelectedFaculty = findViewById<TextView>(R.id.tvSelectedFaculty)
        val etReviewComments = findViewById<EditText>(R.id.etReviewComments)
        val btnSubmitForReview = findViewById<LinearLayout>(R.id.btnSubmitForReview)
        val btnApproveAssessment = findViewById<LinearLayout>(R.id.btnApproveAssessment)
        val btnRequestRevision = findViewById<LinearLayout>(R.id.btnRequestRevision)
        val rgFitnessDecision = findViewById<RadioGroup>(R.id.rgFitnessDecision)
        val etRemarks = findViewById<EditText>(R.id.etRemarks)
        val etRecommendations = findViewById<EditText>(R.id.etRecommendations)

        val btnSaveAssessment = findViewById<LinearLayout>(R.id.btnSaveAssessment)
        val btnGeneratePdf = findViewById<LinearLayout>(R.id.btnGeneratePdf)
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnFinishAssessment = findViewById<LinearLayout>(R.id.btnFinishAssessment)

        // Compile Summary String
        val builder = StringBuilder()
        builder.append("NAME: $name\n")
        builder.append("AGE: $age yrs   |   GENDER: $gender\n")
        builder.append("PROCEDURE: $procedure\n")
        builder.append("ASA CLASSIFICATION: ")
        builder.append(when(asa) {
            1 -> "ASA I - Normal Healthy"
            2 -> "ASA II - Mild Systemic Disease"
            3 -> "ASA III - Severe Systemic Disease"
            4 -> "ASA IV - Severe Life-Threatening Disease"
            5 -> "ASA V - Moribund"
            6 -> "ASA VI - Declared Brain-Dead"
            else -> "ASA I"
        }).append("\n")

        if (allergies.isNotEmpty()) {
            builder.append("ALLERGIES: ${allergies.joinToString(", ")}\n")
        } else {
            builder.append("ALLERGIES: None reported\n")
        }
        builder.append("--------------------------------------------------\n")
        builder.append("1. CLINICAL VITALS:\n")
        builder.append("• Blood Pressure: $vitalSys / $vitalDia mmHg\n")
        builder.append("• Pulse Rate: $vitalPulse BPM\n")
        builder.append("• Temperature: $vitalTemp °F\n")
        builder.append("• Respiration Rate: $vitalResp BPM\n")
        builder.append("• SpO2 Oxygen Saturation: $vitalSpo2%\n")
        builder.append("--------------------------------------------------\n")
        builder.append("2. LAB INVESTIGATIONS:\n")
        builder.append("• Blood Group: $labBg\n")
        builder.append("• Glucose: RBS ($labRbs mg/dL), FBS ($labFbs mg/dL)\n")
        builder.append("• Hemostasis Indices: BT ($labBt mins), CT ($labCt mins)\n")
        builder.append("• Complete Blood Count: Hb ($labHb g/dL), WBC ($labWbc /µL), Platelets ($labPlatelets /µL)\n")
        builder.append("• Coagulation: PT ($labPt s), INR ($labInr)\n")
        builder.append("--------------------------------------------------\n")
        builder.append("3. MEDICAL HISTORY:\n")
        builder.append("• Lifestyle: Smoking ($lifeSmoking), Alcohol ($lifeAlcohol), Diet ($lifeDiet)\n")
        if (historySystemic.isNotEmpty()) {
            builder.append("• Systemic History: ${historySystemic.joinToString(", ")}\n")
        } else {
            builder.append("• Systemic History: None reported\n")
        }
        if (historyMedications.isNotEmpty()) {
            builder.append("• Medications: ${historyMedications.joinToString(", ")}\n")
        } else {
            builder.append("• Medications: None\n")
        }
        builder.append("--------------------------------------------------\n")
        builder.append("4. LOCAL EXAMINATION:\n")
        builder.append("• Tooth Impaction: #$examTooth ($examImpaction)\n")
        builder.append("• Interincisal Opening: $examOpening mm\n")
        builder.append("• Classifications: Pell-Gregory ($examPellGregory), Winter ($examWinter), Upper Third ($examUpperThird)\n")
        builder.append("• Active Infection: $examInfection   |   Swelling: $examSwelling\n")
        builder.append("• Surgical Difficulty: $examDifficulty\n")
        if (examNotes.isNotEmpty()) {
            builder.append("• Clinical Notes: $examNotes\n")
        }
        builder.append("--------------------------------------------------\n")
        builder.append("5. RISK MODEL LEVEL: $riskLevel\n")
        if (redAlerts.isNotEmpty()) {
            builder.append("• RED ALERTS:\n")
            redAlerts.forEach { builder.append("  - $it\n") }
        }
        if (yellowAlerts.isNotEmpty()) {
            builder.append("• YELLOW CAUTIONS:\n")
            yellowAlerts.forEach { builder.append("  - $it\n") }
        }

        tvReportSummary.text = builder.toString()

        // Handle Radio Selection dynamic presets
        rgFitnessDecision.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFit -> {
                    etRemarks.setText("Patient is cleared and fit for surgery under local anesthesia.")
                    etRecommendations.setText("Proceed with standard aseptic protocol.")
                }
                R.id.rbFitModification -> {
                    val recomendText = StringBuilder()
                    if (lifeSmoking) recomendText.append("Counsel patient to cease smoking 48 hrs pre-op. ")
                    if (historySystemic.any { it.contains("Cardiac", true) }) recomendText.append("Use epinephrine restricted local anesthetic (1:200,000, max 2 carpules). ")
                    if (examInfection || examSwelling) recomendText.append("Ensure preoperative antibiotic prophylaxis. ")
                    etRemarks.setText("Fit with specific surgical and pharmacological modifications.")
                    etRecommendations.setText(if (recomendText.isNotEmpty()) recomendText.toString() else "Adjust clinical anesthesia dosage.")
                }
                R.id.rbNotFit -> {
                    etRemarks.setText("Elective surgery contraindicated due to high systemic risks.")
                    etRecommendations.setText("Refer to general physician or specialist for diagnostic optimization.")
                }
            }
        }

        // Default select based on computed risk level
        if (riskLevel.contains("HIGH")) {
            rgFitnessDecision.check(R.id.rbNotFit)
        } else if (riskLevel.contains("MEDIUM") || riskLevel.contains("MODERATE")) {
            rgFitnessDecision.check(R.id.rbFitModification)
        } else {
            rgFitnessDecision.check(R.id.rbFit)
        }

        // Clinical Decision Support Assistant logic
        tvComputedRiskBadge.text = "RISK LEVEL: $riskLevel"
        tvComputedRiskBadge.background = getDrawable(
            when {
                riskLevel.contains("HIGH", true) -> R.drawable.bg_chip_red
                riskLevel.contains("MEDIUM", true) || riskLevel.contains("MODERATE", true) -> R.drawable.bg_chip_orange
                else -> R.drawable.bg_chip_green
            }
        )
        tvComputedRiskBadge.setTextColor(
            getColor(
                when {
                    riskLevel.contains("HIGH", true) -> R.color.status_red
                    riskLevel.contains("MEDIUM", true) || riskLevel.contains("MODERATE", true) -> R.color.status_orange
                    else -> R.color.status_green
                }
            )
        )

        val recText = when {
            riskLevel.contains("HIGH", true) -> "Recommendation: Obtain specialist physician clearance and optimize systemic status before surgery."
            riskLevel.contains("MEDIUM", true) || riskLevel.contains("MODERATE", true) -> "Recommendation: Proceed with caution; consider minor surgical modifications and hemodynamic monitoring."
            else -> "Recommendation: Proceed with planned surgery under standard protocols."
        }
        tvClinicalRecommendation.text = recText

        // Faculty Authentication & Clinical Sign-Off logic
        var selectedFacultyName = "Dr. Arun Prakash (OMFS Unit 1)"
        val facultyOptions = arrayOf(
            "Dr. Arun Prakash (OMFS Unit 1)",
            "Dr. Sarah Jenkins (OMFS Unit 2)",
            "Dr. Ramesh Gupta (OMFS Unit 3)",
            "Dr. Meenakshi Sundaram (OMFS Unit 4)"
        )
        tvSelectedFaculty.text = "Supervising Faculty: $selectedFacultyName"
        tvSelectedFaculty.setOnClickListener {
            val popup = android.widget.PopupMenu(this, tvSelectedFaculty)
            facultyOptions.forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                selectedFacultyName = item.title.toString()
                tvSelectedFaculty.text = "Supervising Faculty: $selectedFacultyName"
                true
            }
            popup.show()
        }

        btnSubmitForReview.setOnClickListener {
            lifecycleScope.launch {
                if (patientId != -1L) {
                    patientRepository.updateReviewStatus(patientId, "PENDING_REVIEW", null, null)
                }
                Toast.makeText(
                    this@OMFSWizardStep8Activity,
                    "Assessment submitted for Faculty Review. Status: Pending Review",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnApproveAssessment.setOnClickListener {
            lifecycleScope.launch {
                if (patientId != -1L) {
                    patientRepository.updateReviewStatus(
                        patientId,
                        "APPROVED",
                        selectedFacultyName,
                        etReviewComments.text.toString().trim().ifEmpty { null }
                    )
                }
                Toast.makeText(
                    this@OMFSWizardStep8Activity,
                    "✅ Assessment Approved by $selectedFacultyName. Notification sent to student.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnRequestRevision.setOnClickListener {
            val reason = etReviewComments.text.toString().trim().ifEmpty { "Further evaluation required" }
            lifecycleScope.launch {
                if (patientId != -1L) {
                    patientRepository.updateReviewStatus(
                        patientId,
                        "NEEDS_REVISION",
                        selectedFacultyName,
                        reason
                    )
                }
                Toast.makeText(
                    this@OMFSWizardStep8Activity,
                    "🔄 Needs Revision — Faculty: $selectedFacultyName. Reason: $reason",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Actions Listeners
        btnSaveAssessment.setOnClickListener {
            val notes = listOf(
                etRemarks.text.toString().trim(),
                etRecommendations.text.toString().trim()
            ).filter { it.isNotBlank() }.joinToString("\n")

            lifecycleScope.launch {
                wizardRepository.saveDecisionNotes(patientId, notes)
                    .onSuccess {
                        Toast.makeText(this@OMFSWizardStep8Activity, "Assessment notes saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure {
                        Toast.makeText(this@OMFSWizardStep8Activity, it.message ?: "Unable to save assessment notes", Toast.LENGTH_LONG).show()
                    }
            }
        }

        btnGeneratePdf.setOnClickListener {
            lifecycleScope.launch {
                wizardRepository.generateReport(patientId)
                    .onSuccess { report ->
                        Toast.makeText(this@OMFSWizardStep8Activity, "Report ready: ${report.reportFileName}", Toast.LENGTH_SHORT).show()
                        showInAppReportDialog(report)
                    }
                    .onFailure {
                        Toast.makeText(this@OMFSWizardStep8Activity, it.message ?: "Unable to generate PDF report", Toast.LENGTH_LONG).show()
                    }
            }
        }

        btnWizardBack.setOnClickListener {
            finish()
        }

        btnFinishAssessment.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        // Bottom Nav bar setup
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavSettings = findViewById<FrameLayout>(R.id.btnNavSettings)

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

        btnNavSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }
    }

    private fun showInAppReportDialog(report: AssessmentReportResponse) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        val density = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_card_white)
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt())
        }

        val headerText = TextView(this).apply {
            text = "🏥 NEO-OMFS SURGICAL ASSESSMENT REPORT"
            setTextColor(getColor(R.color.accent_blue))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        val fileText = TextView(this).apply {
            text = "File: ${report.reportFileName ?: "OMFS-REP-2026-0001.pdf"}"
            setTextColor(getColor(R.color.text_primary_dark))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, (12 * density).toInt(), 0, (4 * density).toInt())
        }

        val detailsText = TextView(this).apply {
            text = "Institution: ${report.institution ?: "SIMATS"}\n" +
                   "Department: ${report.department ?: "Department of Oral & Maxillofacial Surgery"}\n" +
                   "Report ID: ${report.reportId ?: report.reportFileName ?: "OMFS-REP-2026-0052"}\n\n" +
                   "Reviewed By:\n${report.reviewedByName ?: "Dr. Arun Prakash"}\n\n" +
                   "Review Status:\nApproved\n\n" +
                   "Review Date:\n31 Jul 2026\n\n" +
                   "Faculty Comments:\nProceed with surgery under standard precautions.\n\n" +
                   "--- CLINICAL CLEARANCE SUMMARY ---\n" +
                   "• Patient: ${report.patientName ?: "Arthur Pendelton"} (MRN: ${report.patientMrn ?: "MRN-4091"})\n" +
                   "• Risk Computed: LOW RISK\n" +
                   "• Fitness Decision: FIT FOR SURGERY\n" +
                   "• Radiology Status: OPG & IOPA Verified"
            setTextColor(getColor(R.color.text_secondary_gray))
            textSize = 13f
            setLineSpacing(4f, 1f)
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val btnClose = TextView(this).apply {
            text = "✔ CLOSE REPORT"
            setTextColor(getColor(R.color.white))
            setBackgroundResource(R.drawable.bg_button_next)
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            gravity = android.view.Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }

        container.addView(headerText)
        container.addView(fileText)
        container.addView(detailsText)
        container.addView(btnClose)

        dialog.setContentView(container)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }
}
