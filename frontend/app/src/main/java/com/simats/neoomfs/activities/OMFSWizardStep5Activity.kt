package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.models.MedicalHistoryRequest
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.WizardRepository
import kotlinx.coroutines.launch

class OMFSWizardStep5Activity : AppCompatActivity() {

    private val wizardRepository = WizardRepository()
    private val currentMedications = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step5)

        RetrofitClient.initialize(applicationContext)

        // Retrieve intent extras
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

        // Lifestyle Views
        val cbSmoking = findViewById<CheckBox>(R.id.cbSmoking)
        val cbAlcohol = findViewById<CheckBox>(R.id.cbAlcohol)
        val spinnerDiet = findViewById<RelativeLayout>(R.id.spinnerDiet)
        val tvSelectedDiet = findViewById<TextView>(R.id.tvSelectedDiet)

        // Medical History Checkboxes
        val cbDiabetes = findViewById<CheckBox>(R.id.cbDiabetes)
        val cbHypertension = findViewById<CheckBox>(R.id.cbHypertension)
        val cbAsthma = findViewById<CheckBox>(R.id.cbAsthma)
        val cbCardiac = findViewById<CheckBox>(R.id.cbCardiac)
        val cbBleeding = findViewById<CheckBox>(R.id.cbBleeding)
        val cbKidney = findViewById<CheckBox>(R.id.cbKidney)
        val cbLiver = findViewById<CheckBox>(R.id.cbLiver)
        val cbPregnancy = findViewById<CheckBox>(R.id.cbPregnancy)
        val cbDrugAllergy = findViewById<CheckBox>(R.id.cbDrugAllergy)
        val etAllergyDetails = findViewById<EditText>(R.id.etAllergyDetails)

        // Medications Dynamic Section
        val etDrugName = findViewById<EditText>(R.id.etDrugName)
        val etDrugDosage = findViewById<EditText>(R.id.etDrugDosage)
        val etDrugFrequency = findViewById<EditText>(R.id.etDrugFrequency)
        val btnAddMedication = findViewById<LinearLayout>(R.id.btnAddMedication)
        val layoutMedicationsList = findViewById<LinearLayout>(R.id.layoutMedicationsList)

        // Educational Expandables
        val btnWhyLifestyle = findViewById<RelativeLayout>(R.id.btnWhyLifestyle)
        val layoutWhyLifestyle = findViewById<LinearLayout>(R.id.layoutWhyLifestyle)
        val btnWhyMedicalHistory = findViewById<RelativeLayout>(R.id.btnWhyMedicalHistory)
        val layoutWhyMedicalHistory = findViewById<LinearLayout>(R.id.layoutWhyMedicalHistory)
        val btnWhyMedications = findViewById<RelativeLayout>(R.id.btnWhyMedications)
        val layoutWhyMedications = findViewById<LinearLayout>(R.id.layoutWhyMedications)

        fun setupToggle(trigger: View, target: View) {
            trigger.setOnClickListener {
                target.visibility = if (target.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
        setupToggle(btnWhyLifestyle, layoutWhyLifestyle)
        setupToggle(btnWhyMedicalHistory, layoutWhyMedicalHistory)
        setupToggle(btnWhyMedications, layoutWhyMedications)

        // Show/hide allergy edittext
        cbDrugAllergy.setOnCheckedChangeListener { _, isChecked ->
            etAllergyDetails.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Diet Spinner Popup
        val openDietMenu = {
            val popup = PopupMenu(this, spinnerDiet)
            popup.menu.add("Normal Mixed Diet")
            popup.menu.add("Soft Diet")
            popup.menu.add("Liquid Diet")
            popup.menu.add("Diabetic Diet")
            popup.menu.add("Low Sodium Diet")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedDiet.text = item.title
                true
            }
            popup.show()
        }
        spinnerDiet.setOnClickListener { openDietMenu() }
        tvSelectedDiet.setOnClickListener { openDietMenu() }

        // Add dynamic medication logic
        btnAddMedication.setOnClickListener {
            val drug = etDrugName.text.toString().trim()
            val dosage = etDrugDosage.text.toString().trim()
            val freq = etDrugFrequency.text.toString().trim()

            if (drug.isEmpty()) {
                Toast.makeText(this, "Please enter drug name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val medRecord = "$drug ($dosage, $freq)"
            if (currentMedications.contains(medRecord)) {
                Toast.makeText(this, "Medication already added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create view
            val density = resources.displayMetrics.density
            val newRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (44 * density).toInt()
                ).apply {
                    bottomMargin = (8 * density).toInt()
                }
                background = getDrawable(R.drawable.bg_card_active_blue)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)
            }

            val tvMedInfo = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                text = medRecord
                setTextColor(getColor(R.color.accent_blue))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            }

            val tvRemove = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                text = "✕ "
                setTextColor(getColor(R.color.status_red))
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding((8 * density).toInt(), 0, (8 * density).toInt(), 0)
                setOnClickListener {
                    layoutMedicationsList.removeView(newRow)
                    currentMedications.remove(medRecord)
                }
            }

            newRow.addView(tvMedInfo)
            newRow.addView(tvRemove)
            layoutMedicationsList.addView(newRow)
            currentMedications.add(medRecord)

            // Clear entries
            etDrugName.text.clear()
            etDrugDosage.text.clear()
            etDrugFrequency.text.clear()
        }

        // Navigation
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)

        btnWizardBack.setOnClickListener {
            finish()
        }

        btnWizardNext.setOnClickListener {
            // Bundle lifestyle values
            val lifestyleSmoking = cbSmoking.isChecked
            val lifestyleAlcohol = cbAlcohol.isChecked
            val lifestyleDiet = tvSelectedDiet.text.toString()

            // Bundle medical histories
            val historyList = ArrayList<String>()
            if (cbDiabetes.isChecked) historyList.add("Diabetes")
            if (cbHypertension.isChecked) historyList.add("Hypertension")
            if (cbAsthma.isChecked) historyList.add("Asthma")
            if (cbCardiac.isChecked) historyList.add("Cardiac Disease")
            if (cbBleeding.isChecked) historyList.add("Bleeding Disorder")
            if (cbKidney.isChecked) historyList.add("Kidney Disease")
            if (cbLiver.isChecked) historyList.add("Liver Disease")
            if (cbPregnancy.isChecked) historyList.add("Pregnancy")
            if (cbDrugAllergy.isChecked) {
                val detail = etAllergyDetails.text.toString().trim()
                if (detail.isNotEmpty()) {
                    historyList.add("Drug Allergy: $detail")
                } else {
                    historyList.add("Drug Allergy")
                }
            }

            val request = MedicalHistoryRequest(
                hypertension = cbHypertension.isChecked,
                diabetes = cbDiabetes.isChecked,
                heartDisease = cbCardiac.isChecked,
                kidneyDisease = cbKidney.isChecked,
                liverDisease = cbLiver.isChecked,
                asthma = cbAsthma.isChecked,
                bloodDisorder = cbBleeding.isChecked,
                pregnant = cbPregnancy.isChecked,
                otherConditions = historyList.joinToString(", "),
                currentMedications = currentMedications.joinToString(", "),
                allergies = if (cbDrugAllergy.isChecked) etAllergyDetails.text.toString().trim() else allergies.joinToString(", "),
                socialHistory = listOf(
                    if (lifestyleSmoking) "Smoking" else null,
                    if (lifestyleAlcohol) "Alcohol" else null,
                    lifestyleDiet.takeIf { it.isNotBlank() }
                ).filterNotNull().joinToString(", ")
            )

            lifecycleScope.launch {
                wizardRepository.saveMedicalHistory(patientId, request)
                    .onSuccess {
                        val intent = Intent(this@OMFSWizardStep5Activity, OMFSWizardStep6Activity::class.java).apply {
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
                putExtra("life_smoking", lifestyleSmoking)
                putExtra("life_alcohol", lifestyleAlcohol)
                putExtra("life_diet", lifestyleDiet)
                putStringArrayListExtra("history_systemic", historyList)
                putStringArrayListExtra("history_medications", ArrayList(currentMedications))
                        }
                        startActivity(intent)
                    }
                    .onFailure {
                        Toast.makeText(this@OMFSWizardStep5Activity, it.message ?: "Unable to save medical history", Toast.LENGTH_LONG).show()
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
