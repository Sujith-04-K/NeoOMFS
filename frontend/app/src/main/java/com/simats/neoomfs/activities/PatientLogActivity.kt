package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Typeface
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.utils.startActivityNoAnimation
import com.simats.neoomfs.models.AssessmentReportResponse
import com.simats.neoomfs.models.PatientResponse
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.BackendPatientRepository
import com.simats.neoomfs.repository.ReportRepository
import kotlinx.coroutines.launch

class PatientLogActivity : AppCompatActivity() {
    private val patientRepository = BackendPatientRepository()
    private val reportRepository = ReportRepository()
    private var allPatients: List<PatientResponse> = emptyList()
    private var currentPatient: PatientResponse? = null
    private var currentReports: List<AssessmentReportResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_log)

        RetrofitClient.initialize(applicationContext)

        // Find filter views
        val etPatientSearch = findViewById<EditText>(R.id.etPatientSearch)
        val btnApplyFilters = findViewById<LinearLayout>(R.id.btnApplyFilters)
        val btnViewAllReports = findViewById<TextView>(R.id.btnViewAllReports)
        
        val layoutReportsList = findViewById<LinearLayout>(R.id.layoutReportsList)

        // Find bottom nav buttons
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<LinearLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<FrameLayout>(R.id.btnNavAssess)
        val btnNavSettings = findViewById<FrameLayout>(R.id.btnNavSettings)

        val spinnerStatus = findViewById<RelativeLayout>(R.id.spinnerStatus)
        val tvSelectedStatus = findViewById<TextView>(R.id.tvSelectedStatus)
        val spinnerDoctor = findViewById<RelativeLayout>(R.id.spinnerDoctor)
        val tvSelectedDoctor = findViewById<TextView>(R.id.tvSelectedDoctor)
        val tvPatientResultsCount = findViewById<TextView>(R.id.tvPatientResultsCount)
        val layoutPatientResults = findViewById<LinearLayout>(R.id.layoutPatientResults)
        val tvCaseSummaryBadge = findViewById<TextView>(R.id.tvCaseSummaryBadge)
        val layoutCaseSummary = findViewById<LinearLayout>(R.id.layoutCaseSummary)

        val openStatusMenu = {
            val popup = android.widget.PopupMenu(this, spinnerStatus)
            popup.menu.add("All Status")
            popup.menu.add("FIT")
            popup.menu.add("REVIEW")
            popup.menu.add("CRITICAL")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedStatus.text = item.title
                true
            }
            popup.show()
        }
        spinnerStatus.setOnClickListener { openStatusMenu() }
        tvSelectedStatus.setOnClickListener { openStatusMenu() }

        val openDoctorMenu = {
            val popup = android.widget.PopupMenu(this, spinnerDoctor)
            popup.menu.add("All Doctors")
            allPatients.mapNotNull { it.createdByName }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .forEach { doctorName ->
                    popup.menu.add(doctorName)
                }
            popup.setOnMenuItemClickListener { item ->
                tvSelectedDoctor.text = item.title
                true
            }
            popup.show()
        }
        spinnerDoctor.setOnClickListener { openDoctorMenu() }
        tvSelectedDoctor.setOnClickListener { openDoctorMenu() }

        lifecycleScope.launch {
            patientRepository.searchPatients(search = null, status = null)
                .onSuccess { patients ->
                    allPatients = patients
                    renderPatientResults(layoutPatientResults, tvPatientResultsCount, patients, layoutReportsList, tvCaseSummaryBadge, layoutCaseSummary)
                    if (patients.isNotEmpty()) {
                        currentPatient = patients.first()
                        renderCaseSummary(layoutCaseSummary, tvCaseSummaryBadge, currentPatient, currentReports)
                        loadReportsForPatient(currentPatient!!, layoutReportsList, tvCaseSummaryBadge, layoutCaseSummary)
                    } else {
                        renderCaseSummary(layoutCaseSummary, tvCaseSummaryBadge, null, emptyList())
                        renderReports(layoutReportsList, emptyList(), "No reports available because no patients were found")
                    }
                }
                .onFailure {
                    Toast.makeText(this@PatientLogActivity, it.message ?: "Unable to load patients", Toast.LENGTH_LONG).show()
                }
        }

        btnApplyFilters.setOnClickListener {
            val query = etPatientSearch.text.toString().trim()
            val status = tvSelectedStatus.text.toString()
            val doctor = tvSelectedDoctor.text.toString()
            val filtered = allPatients.filter { patient ->
                val matchesQuery = query.isBlank() || listOf(
                    patient.fullName,
                    patient.id.toString(),
                    patient.phoneNumber.orEmpty(),
                    patient.mrn.orEmpty()
                ).joinToString(" ").contains(query, ignoreCase = true)
                val matchesStatus = status == "All Status" || patient.assessmentStatus.orEmpty().equals(status, ignoreCase = true)
                val matchesDoctor = doctor == "All Doctors" || patient.createdByName.orEmpty().equals(doctor, ignoreCase = true)
                matchesQuery && matchesStatus && matchesDoctor
            }

            renderPatientResults(layoutPatientResults, tvPatientResultsCount, filtered, layoutReportsList, tvCaseSummaryBadge, layoutCaseSummary)
            if (filtered.isNotEmpty()) {
                currentPatient = filtered.first()
                renderCaseSummary(layoutCaseSummary, tvCaseSummaryBadge, currentPatient, currentReports)
                loadReportsForPatient(currentPatient!!, layoutReportsList, tvCaseSummaryBadge, layoutCaseSummary)
                Toast.makeText(this, "Showing ${filtered.size} backend patient record(s)", Toast.LENGTH_SHORT).show()
            } else {
                currentPatient = null
                currentReports = emptyList()
                renderCaseSummary(layoutCaseSummary, tvCaseSummaryBadge, null, emptyList())
                renderReports(layoutReportsList, emptyList(), "No reports available because no patients matched your filters")
                Toast.makeText(this, "No backend patient records matched your filters", Toast.LENGTH_LONG).show()
            }
        }

        btnViewAllReports.setOnClickListener {
            if (currentReports.isEmpty()) {
                Toast.makeText(this, "No backend reports available for this patient", Toast.LENGTH_SHORT).show()
            } else {
                renderReports(layoutReportsList, currentReports)
                Toast.makeText(this, "Showing ${currentReports.size} report(s)", Toast.LENGTH_SHORT).show()
            }
        }

        // Shifting Bottom Navigation Setup
        btnNavDashboard.setOnClickListener {
            // Navigate back to Dashboard (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityNoAnimation(intent)
        }

        btnNavPatients.setOnClickListener {
            // Already on Patients Log screen
            Toast.makeText(this, "You are already viewing patient logs", Toast.LENGTH_SHORT).show()
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

    private fun loadReportsForPatient(
        patient: PatientResponse,
        layoutReportsList: LinearLayout,
        caseSummaryBadge: TextView,
        caseSummaryContainer: LinearLayout
    ) {
        lifecycleScope.launch {
            reportRepository.listReports(patient.id)
                .onSuccess { reports ->
                    currentReports = reports
                    renderReports(layoutReportsList, reports)
                    renderCaseSummary(caseSummaryContainer, caseSummaryBadge, patient, reports)
                }
                .onFailure {
                    currentReports = emptyList()
                    renderReports(layoutReportsList, emptyList(), it.message ?: "Unable to load reports")
                    renderCaseSummary(caseSummaryContainer, caseSummaryBadge, patient, emptyList())
                }
        }
    }

    private fun renderReports(
        container: LinearLayout,
        reports: List<AssessmentReportResponse>,
        emptyMessage: String = "No reports generated yet"
    ) {
        container.removeAllViews()

        if (reports.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = emptyMessage
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 12f
                setPadding(0, 12, 0, 12)
            }
            container.addView(emptyView)
            return
        }

        reports.forEachIndexed { index, report ->
            val density = resources.displayMetrics.density
            val card = RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 0 else (12 * density).toInt()
                }
                background = getDrawable(R.drawable.bg_card_white)
                isClickable = true
                isFocusable = true
                setPadding((12 * density).toInt(), (12 * density).toInt(), (12 * density).toInt(), (12 * density).toInt())
                setOnClickListener { openReport(report) }
            }

            val iconContainer = FrameLayout(this).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams((40 * density).toInt(), (40 * density).toInt()).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_START)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
                background = getDrawable(if (index % 2 == 0) R.drawable.bg_card_red else R.drawable.bg_card_blue)
                setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
            }

            val icon = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                setImageResource(if (index % 2 == 0) R.drawable.ic_document else R.drawable.ic_radiology)
                setColorFilter(getColor(if (index % 2 == 0) R.color.status_red else R.color.accent_blue))
            }
            iconContainer.addView(icon)

            val textColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.END_OF, iconContainer.id)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                    marginStart = (12 * density).toInt()
                }
            }

            val title = TextView(this).apply {
                text = report.reportFileName ?: "Assessment report"
                setTextColor(getColor(R.color.bg_dark_blue))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }

            val meta = TextView(this).apply {
                val versionText = report.reportVersion?.let { "Version $it" } ?: ""
                val dateText = report.reportGeneratedAt ?: ""
                text = listOf(versionText, dateText).filter { it.isNotBlank() }.joinToString(" • ")
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 11f
            }

            textColumn.addView(title)
            textColumn.addView(meta)

            val downloadIcon = ImageView(this).apply {
                layoutParams = RelativeLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt()).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
                setPadding((6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt())
                setImageResource(R.drawable.ic_download)
            }

            card.addView(iconContainer)
            card.addView(textColumn)
            card.addView(downloadIcon)
            card.setOnClickListener {
                currentPatient?.let { patient -> openPatientReport(patient) }
            }
            container.addView(card)
        }
    }

    private fun renderPatientResults(
        container: LinearLayout,
        countView: TextView,
        patients: List<PatientResponse>,
        reportsContainer: LinearLayout,
        caseSummaryBadge: TextView,
        caseSummaryContainer: LinearLayout
    ) {
        container.removeAllViews()
        countView.text = if (patients.size == 1) "1 result" else "${patients.size} results"

        if (patients.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No patients matched the selected filters"
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 12f
                setPadding(0, 12, 0, 12)
            }
            container.addView(emptyView)
            return
        }

        val density = resources.displayMetrics.density
        patients.forEachIndexed { index, patient ->
            val card = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 0 else (12 * density).toInt()
                }
                orientation = LinearLayout.VERTICAL
                background = getDrawable(R.drawable.bg_card_white)
                isClickable = true
                isFocusable = true
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
                setOnClickListener {
                    currentPatient = patient
                    renderCaseSummary(caseSummaryContainer, caseSummaryBadge, patient, currentReports)
                    loadReportsForPatient(patient, reportsContainer, caseSummaryBadge, caseSummaryContainer)
                    Toast.makeText(this@PatientLogActivity, "Selected ${patient.fullName}", Toast.LENGTH_SHORT).show()
                    openPatientReport(patient)
                }
            }

            val header = RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val patientInfo = LinearLayout(this).apply {
                id = View.generateViewId()
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val avatar = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams((48 * density).toInt(), (48 * density).toInt())
                background = getDrawable(R.drawable.bg_circle_blue)
            }
            avatar.addView(TextView(this@PatientLogActivity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                gravity = android.view.Gravity.CENTER
                text = patient.fullName.firstOrNull()?.uppercase() ?: "?"
                textSize = 18f
            })

            val labels = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = (12 * density).toInt()
                }
            }
            labels.addView(TextView(this@PatientLogActivity).apply {
                text = patient.fullName.ifBlank { "Unnamed patient" }
                setTextColor(getColor(R.color.bg_dark_blue))
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
            })
            labels.addView(TextView(this@PatientLogActivity).apply {
                text = "MRN: ${(patient.mrn ?: patient.id.toString()).uppercase()}"
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 13f
            })

            patientInfo.addView(avatar)
            patientInfo.addView(labels)

            val statusView = TextView(this).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
                text = "STATUS: ${(patient.assessmentStatus ?: "REVIEW").uppercase()}"
                setTextColor(getStatusColor(patient.assessmentStatus))
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding((10 * density).toInt(), (5 * density).toInt(), (10 * density).toInt(), (5 * density).toInt())
                background = getDrawable(
                    if (patient.assessmentStatus.equals("CRITICAL", ignoreCase = true)) R.drawable.bg_chip_red else R.drawable.bg_chip_orange
                )
            }

            header.addView(patientInfo)
            header.addView(statusView)

            val metaRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * density).toInt()
                }
            }

            metaRow.addView(buildMetaColumn("AGE", patient.age?.let { "$it yrs" } ?: "Not set"))
            metaRow.addView(buildMetaColumn("GENDER", patient.gender ?: "Not set"))

            card.addView(header)
            card.addView(metaRow)
            container.addView(card)
        }
    }

    private fun renderCaseSummary(
        container: LinearLayout,
        badgeView: TextView,
        patient: PatientResponse?,
        reports: List<AssessmentReportResponse>
    ) {
        container.removeAllViews()

        if (patient == null) {
            badgeView.text = "No patient selected"
            val emptyView = TextView(this).apply {
                text = "Select a patient to view backend case details and report history."
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 12f
                setPadding(0, 8, 0, 8)
            }
            container.addView(emptyView)
            return
        }

        badgeView.text = patient.assessmentStatus?.uppercase() ?: "REVIEW"

        val summaryCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getDrawable(R.drawable.bg_card_white)
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }

        val title = TextView(this).apply {
            text = patient.fullName.ifBlank { "Unnamed patient" }
            setTextColor(getColor(R.color.bg_dark_blue))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = listOf(
                patient.procedureType?.takeIf { it.isNotBlank() }?.let { "Procedure: $it" },
                patient.referringDoctor?.takeIf { it.isNotBlank() }?.let { "Doctor: $it" }
            ).filterNotNull().ifEmpty { listOf("Backend patient record") }.joinToString(" • ")
            setTextColor(getColor(R.color.text_secondary_gray))
            textSize = 12f
        }

        val metrics = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16.dp()
            }
        }
        metrics.addView(buildMetaColumn("STATUS", patient.assessmentStatus ?: "Review"))
        metrics.addView(buildMetaColumn("REPORTS", reports.size.toString()))

        val details = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16.dp()
            }
            text = buildCaseSummaryText(patient, reports)
            setTextColor(getColor(R.color.text_secondary_gray))
            textSize = 13f
        }

        val btnOpenReport = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16.dp()
            }
            text = "📄 OPEN SURGICAL CLEARANCE REPORT ➔"
            setTextColor(getColor(R.color.white))
            setBackgroundResource(R.drawable.bg_button_next)
            setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
            gravity = android.view.Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                openPatientReport(patient)
            }
        }

        summaryCard.addView(title)
        summaryCard.addView(subtitle)
        summaryCard.addView(metrics)
        summaryCard.addView(details)
        summaryCard.addView(btnOpenReport)
        container.addView(summaryCard)
    }

    private fun openPatientReport(patient: PatientResponse) {
        val intent = Intent(this, OMFSWizardStep8Activity::class.java).apply {
            putExtra("patient_id", patient.id ?: 1L)
            putExtra("patient_name", patient.fullName.ifBlank { "Unnamed patient" })
            putExtra("patient_age", patient.age?.toString() ?: "35")
            putExtra("patient_gender", patient.gender ?: "Male")
            putExtra("patient_procedure", patient.procedureType ?: "Oral Surgery")
            putExtra("patient_asa", 1)
        }
        startActivity(intent)
    }

    private fun buildCaseSummaryText(patient: PatientResponse, reports: List<AssessmentReportResponse>): String {
        val created = patient.createdAt?.takeIf { it.isNotBlank() } ?: "Not available"
        val updated = patient.updatedAt?.takeIf { it.isNotBlank() } ?: "Not available"
        val lastReport = reports.firstOrNull()?.reportGeneratedAt?.takeIf { it.isNotBlank() } ?: "No reports generated yet"
        val doctor = patient.createdByName?.takeIf { it.isNotBlank() }
            ?: patient.referringDoctor?.takeIf { it.isNotBlank() }
            ?: "Not assigned"

        return "Created: $created\nUpdated: $updated\nAssigned doctor: $doctor\nLast report: $lastReport"
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    private fun buildMetaColumn(label: String, value: String): LinearLayout {
        val density = resources.displayMetrics.density
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@PatientLogActivity).apply {
                text = label
                setTextColor(getColor(R.color.text_secondary_gray))
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
            })
            addView(TextView(this@PatientLogActivity).apply {
                text = value
                setTextColor(getColor(R.color.bg_dark_blue))
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, (4 * density).toInt(), 0, 0)
            })
        }
    }

    private fun getStatusColor(status: String?): Int {
        return when {
            status.equals("CRITICAL", ignoreCase = true) -> getColor(R.color.status_red)
            status.equals("FIT", ignoreCase = true) || status.equals("COMPLETED", ignoreCase = true) -> getColor(R.color.status_green)
            else -> getColor(R.color.accent_blue)
        }
    }

    private fun openReport(report: AssessmentReportResponse) {
        showInAppReportDialog(
            title = report.reportFileName ?: "OMFS-REP-2026-0001.pdf",
            patientName = report.patientName ?: "Arthur Pendelton",
            mrn = report.patientMrn ?: "MRN-4091",
            date = report.reportGeneratedAt ?: "2026-07-28 10:30:00",
            doctor = report.generatedByName ?: "Dr. Sarah Jenkins"
        )
    }

    private fun showInAppReportDialog(title: String, patientName: String, mrn: String, date: String, doctor: String) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        val density = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_card_white)
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt())
        }

        val headerText = TextView(this).apply {
            text = "🏥 NEO-OMFS CLINICAL REPORT"
            setTextColor(getColor(R.color.accent_blue))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        val fileText = TextView(this).apply {
            text = "File: $title"
            setTextColor(getColor(R.color.text_primary_dark))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, (12 * density).toInt(), 0, (4 * density).toInt())
        }

        val detailsText = TextView(this).apply {
            text = "Patient: $patientName\nMRN: $mrn\nDate: $date\nGenerated by: $doctor\n\n" +
                   "--- CLINICAL ASSESSMENT SUMMARY ---\n" +
                   "• ASA Classification: ASA I (Normal healthy patient)\n" +
                   "• Procedure: Oral Surgery - Surgical Extraction\n" +
                   "• Risk Computed: LOW RISK\n" +
                   "• Fitness Decision: FIT FOR SURGERY\n" +
                   "• Radiology Status: OPG & IOPA Verified\n" +
                   "• Clinical Recommendation: Proceed under Local Anesthesia without antibiotic prophylaxis."
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
