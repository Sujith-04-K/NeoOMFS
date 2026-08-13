package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.WizardRepository
import kotlinx.coroutines.launch

class OMFSWizardStep7Activity : AppCompatActivity() {
    private val wizardRepository = WizardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step7)

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

        // Layout Containers
        val layoutRedAlerts = findViewById<LinearLayout>(R.id.layoutRedAlerts)
        val layoutYellowAlerts = findViewById<LinearLayout>(R.id.layoutYellowAlerts)
        val layoutClearances = findViewById<LinearLayout>(R.id.layoutClearances)
        val layoutInvestigations = findViewById<LinearLayout>(R.id.layoutInvestigations)

        val tvRiskLevel = findViewById<TextView>(R.id.tvRiskLevel)
        val tvRiskDescription = findViewById<TextView>(R.id.tvRiskDescription)

        // Lists to store calculated summaries
        val redList = mutableListOf<Pair<String, String>>()
        val yellowList = mutableListOf<Pair<String, String>>()
        val clearanceList = mutableListOf<Pair<String, String>>()
        val investigationList = mutableListOf<Pair<String, String>>()

        // Perform evaluations
        // 1. Vitals
        val sysInt = vitalSys.toIntOrNull() ?: 120
        val diaInt = vitalDia.toIntOrNull() ?: 80
        if (sysInt >= 160 || diaInt >= 100) {
            redList.add("Stage II Hypertension" to "Systolic BP $sysInt mmHg / Diastolic BP $diaInt mmHg. High risk of stroke and intraoperative bleeding. Delay elective surgical procedure.")
            clearanceList.add("Cardiology Clearance" to "Patient exhibits Stage II Hypertension. Obtain physician evaluation before proceeding.")
        } else if (sysInt >= 140 || diaInt >= 90) {
            yellowList.add("Stage I Hypertension" to "BP is elevated at $sysInt/$diaInt mmHg. Monitor patient continuously during anesthesia.")
        }

        val pulseInt = vitalPulse.toIntOrNull() ?: 75
        if (pulseInt > 100 || pulseInt < 60) {
            yellowList.add("Heart Rate Abnormality" to "Pulse rate is $pulseInt BPM. Check for systemic fever, anxiety, or intrinsic cardiac disease.")
        }

        val spo2Int = vitalSpo2.toIntOrNull() ?: 98
        if (spo2Int < 95) {
            redList.add("Mild Hypoxia" to "SpO2 is low at $spo2Int%. High anesthetic respiratory risk. Evaluate pulmonary reserves.")
            clearanceList.add("Pulmonology Clearance" to "Oxygen saturation is below 95%. Secure clinical clearing.")
        }

        // 2. Labs
        val rbsInt = labRbs.toIntOrNull() ?: 100
        val fbsInt = labFbs.toIntOrNull() ?: 85
        if (rbsInt > 180 || fbsInt > 130) {
            redList.add("Hyperglycemia Alert" to "RBS is $rbsInt mg/dL (FBS: $fbsInt mg/dL). Extreme risk of surgical site infections and delayed healing. Reschedule elective extraction.")
            clearanceList.add("Diabetologist Clearance" to "Uncontrolled glucose levels. Secure clearance for glycemic control adjustment.")
            investigationList.add("HbA1c Glycated Hemoglobin" to "Determine average glucose control over past 3 months.")
        } else if (rbsInt > 140 || fbsInt > 100) {
            yellowList.add("Borderline Glucose" to "Blood glucose levels are moderately elevated. Ensure aseptic surgical protocol.")
        }

        val btFloat = labBt.toFloatOrNull() ?: 4.0f
        val ctFloat = labCt.toFloatOrNull() ?: 8.0f
        if (btFloat > 7.0f || ctFloat > 15.0f) {
            redList.add("Prolonged Bleeding/Clotting" to "BT is $btFloat mins (CT: $ctFloat mins). Severe clinical hemorrhage risk. Do not attempt surgical extractions.")
            clearanceList.add("Hematologist Evaluation" to "Prolonged clotting times. Bridging therapy or local hemostatics planning required.")
        }

        val hbFloat = labHb.toFloatOrNull() ?: 14.0f
        if (hbFloat < 10.0f) {
            redList.add("Severe Anemia" to "Hemoglobin is $hbFloat g/dL. Decreased oxygen capacity. High cardiovascular workload under anesthesia.")
            clearanceList.add("General Physician Clearance" to "Anemia assessment and iron supplementation/therapy.")
        } else if (hbFloat < 12.0f) {
            yellowList.add("Mild Anemia" to "Hemoglobin is $hbFloat g/dL. Monitor hydration and surgical blood loss.")
        }

        val wbcInt = labWbc.toIntOrNull() ?: 7000
        if (wbcInt > 11000) {
            redList.add("Elevated White Blood Cell Count" to "WBC is $wbcInt cells/µL. Indicates active infection or systemic inflammatory response.")
            investigationList.add("WBC Differential Count" to "Rule out acute neutrophilia or bacterial infections.")
        }

        val plateletsInt = labPlatelets.toIntOrNull() ?: 250000
        if (plateletsInt < 100000) {
            redList.add("Thrombocytopenia Alert" to "Platelet count is low at $plateletsInt /µL. High risk of postsurgical bleeding. Avoid alveolar bone trauma.")
            clearanceList.add("Hematologist Evaluation" to "Thrombocytopenia triage before surgical extraction.")
        }

        val inrFloat = labInr.toFloatOrNull() ?: 1.0f
        if (inrFloat > 1.5f) {
            redList.add("Elevated INR Alert" to "INR is high at $inrFloat. Severely compromised blood clotting capability.")
            clearanceList.add("Physician Clearance (Anticoagulant Bridge)" to "Adjust Warfarin/anticoagulant dosage under cardiologist guidance.")
        }

        // 3. Local Dental Exam
        if (examInfection || examSwelling) {
            yellowList.add("Active Periapical/Local Infection" to "Local swelling or acute infection is active. Local anesthetic block may be ineffective due to acidic tissue pH. Administer preoperative antibiotic therapy.")
        }

        val openingInt = examOpening.toIntOrNull() ?: 40
        if (openingInt < 35) {
            yellowList.add("Severe Trismus" to "Mouth opening is reduced at $openingInt mm. Limits visual visibility and physical instrument clearance.")
        }

        // 4. Systemic Medical History
        if (historySystemic.any { it.contains("Cardiac", true) }) {
            redList.add("Cardiac Pathology" to "History of cardiac disease. epinephrine containing local anesthetics must be restricted (limit to max 2 carpules 1:100k).")
            clearanceList.add("Cardiology Clearance" to "Secure surgical clearance and stress-reduction recommendations.")
            investigationList.add("12-Lead Electrocardiogram (ECG)" to "Mandatory preoperative check to screen for ischemia or arrhythmia.")
        }
        if (historySystemic.any { it.contains("Bleeding", true) }) {
            redList.add("Coagulopathy History" to "Patient has a bleeding disorder history. High risk of prolonged socket bleeding.")
            clearanceList.add("Hematology Consultation" to "Determine need for factor replacement or tranexamic acid washes.")
        }
        if (historySystemic.any { it.contains("Diabetes", true) && !redList.any { it.first.contains("Sugar") } }) {
            yellowList.add("Systemic Diabetes" to "Monitor glucose pre- and post-operatively.")
        }
        if (historySystemic.any { it.contains("Pregnancy", true) }) {
            yellowList.add("Active Pregnancy" to "Restrict dental radiographies unless heavily shielded. Avoid teratogenic prescribing. Limit surgeries to 2nd trimester if urgent.")
            clearanceList.add("Obstetrician Clearance" to "Clearance for surgical stress and local anesthetics safety.")
        }

        // Apply fallback standard items if lists are empty
        if (redList.isEmpty() && yellowList.isEmpty()) {
            tvRiskLevel.text = "LOW RISK"
            tvRiskLevel.setTextColor(getColor(R.color.status_green))
            tvRiskDescription.text = "Patient parameters fall within standard surgical safety margins. Proceed with standard precautions."
        } else if (redList.isNotEmpty()) {
            tvRiskLevel.text = "HIGH RISK"
            tvRiskLevel.setTextColor(getColor(R.color.status_red))
            tvRiskDescription.text = "Crucial clinical parameters exceed safe margins. Elective surgery should be deferred until clearance is secured."
        } else {
            tvRiskLevel.text = "MEDIUM RISK"
            tvRiskLevel.setTextColor(Color.parseColor("#D97706")) // Orange
            tvRiskDescription.text = "Minor clinical warnings are present. Surgical precautions and monitoring are advised."
        }

        // UI rendering helper
        fun renderAlerts(container: LinearLayout, items: List<Pair<String, String>>, bgDrawableId: Int, textColor: Int) {
            val density = resources.displayMetrics.density
            if (items.isEmpty()) {
                val tvNoAlert = TextView(this).apply {
                    text = "No alerts found under this category."
                    setTextColor(getColor(R.color.text_light_gray))
                    textSize = 13f
                    setTypeface(null, Typeface.ITALIC)
                    setPadding(0, (4 * density).toInt(), 0, (8 * density).toInt())
                }
                container.addView(tvNoAlert)
                return
            }

            for (item in items) {
                val card = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = (8 * density).toInt()
                    }
                    background = getDrawable(bgDrawableId)
                    orientation = LinearLayout.VERTICAL
                    setPadding((14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt())
                }

                val title = TextView(this).apply {
                    text = item.first
                    setTextColor(textColor)
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                }

                val desc = TextView(this).apply {
                    text = item.second
                    setTextColor(getColor(R.color.text_secondary_gray))
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = (6 * density).toInt()
                    }
                }

                card.addView(title)
                card.addView(desc)
                container.addView(card)
            }
        }

        // Render Alerts onto UI
        renderAlerts(layoutRedAlerts, redList, R.drawable.bg_card_red, getColor(R.color.status_red))
        renderAlerts(layoutYellowAlerts, yellowList, R.drawable.bg_card_yellow, Color.parseColor("#B45309")) // Dark Orange
        renderAlerts(layoutClearances, clearanceList, R.drawable.bg_card_white, getColor(R.color.accent_blue))
        renderAlerts(layoutInvestigations, investigationList, R.drawable.bg_card_active_blue, getColor(R.color.status_green))

        // Navigation Actions
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)

        btnWizardBack.setOnClickListener {
            finish()
        }

        // Skip button - goes to step 8 with default risk assessment
        val btnWizardSkip = findViewById<android.widget.TextView?>(R.id.btnWizardSkip)
        btnWizardSkip?.setOnClickListener {
            startActivity(Intent(this, OMFSWizardStep8Activity::class.java).apply {
                putExtra("patient_id", patientId)
                putExtra("patient_name", name)
                putExtra("patient_age", age)
                putExtra("patient_gender", gender)
                putExtra("patient_procedure", procedure)
                putExtra("patient_asa", asa)
                putStringArrayListExtra("patient_allergies", allergies)
                putExtra("risk_computed_level", "LOW RISK")
            })
        }

        lifecycleScope.launch {
            wizardRepository.evaluateDecision(patientId)
                .onSuccess { decision ->
                    val riskLevel = decision.riskLevel ?: tvRiskLevel.text.toString()
                    val backendRedAlerts = ArrayList(decision.redAlerts ?: redList.map { "${it.first}: ${it.second}" })
                    val backendYellowAlerts = ArrayList(decision.yellowAlerts ?: yellowList.map { "${it.first}: ${it.second}" })
                    btnWizardNext.setOnClickListener {
                        val intent = Intent(this@OMFSWizardStep7Activity, OMFSWizardStep8Activity::class.java).apply {
                            putExtra("patient_id", patientId)
                            putExtra("patient_name", name)
                putExtra("patient_age", age)
                putExtra("patient_gender", gender)
                putExtra("patient_procedure", procedure)
                putExtra("patient_asa", asa)
                putStringArrayListExtra("patient_allergies", allergies)
                putExtra("vital_bp_sys", vitalSys)
                putExtra("vital_bp_dia", vitalDia)
                putExtra("vital_pulse", vitalPulse)
                putExtra("vital_temp", vitalTemp)
                putExtra("vital_resp", vitalResp)
                putExtra("vital_spo2", vitalSpo2)
                putExtra("lab_blood_group", labBg)
                putExtra("lab_rbs", labRbs)
                putExtra("lab_fbs", labFbs)
                putExtra("lab_bt", labBt)
                putExtra("lab_ct", labCt)
                putExtra("lab_hb", labHb)
                putExtra("lab_wbc", labWbc)
                putExtra("lab_platelets", labPlatelets)
                putExtra("lab_pt", labPt)
                putExtra("lab_inr", labInr)
                putExtra("life_smoking", lifeSmoking)
                putExtra("life_alcohol", lifeAlcohol)
                putExtra("life_diet", lifeDiet)
                putStringArrayListExtra("history_systemic", historySystemic)
                putStringArrayListExtra("history_medications", historyMedications)
                putExtra("exam_mouth_opening", examOpening)
                putExtra("exam_tooth_number", examTooth)
                putExtra("exam_impaction_type", examImpaction)
                putExtra("exam_pell_gregory", examPellGregory)
                putExtra("exam_winter", examWinter)
                putExtra("exam_upper_third", examUpperThird)
                putExtra("exam_swelling", examSwelling)
                putExtra("exam_infection", examInfection)
                putExtra("exam_difficulty", examDifficulty)
                putExtra("exam_notes", examNotes)
                            putExtra("risk_computed_level", riskLevel)
                            putStringArrayListExtra("risk_red_alerts", backendRedAlerts)
                            putStringArrayListExtra("risk_yellow_alerts", backendYellowAlerts)
                        }
                        startActivity(intent)
                    }
                }
                .onFailure { error ->
                    btnWizardNext.setOnClickListener {
                        Toast.makeText(this@OMFSWizardStep7Activity, error.message ?: "Unable to evaluate decision", Toast.LENGTH_LONG).show()
                    }
                }
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
}
