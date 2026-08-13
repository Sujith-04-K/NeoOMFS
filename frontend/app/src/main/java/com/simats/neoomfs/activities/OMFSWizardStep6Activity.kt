package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.simats.neoomfs.models.DentalExaminationRequest
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.WizardRepository
import kotlinx.coroutines.launch

class OMFSWizardStep6Activity : AppCompatActivity() {
    private val wizardRepository = WizardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step6)

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

        // Inputs
        val etMouthOpening = findViewById<EditText>(R.id.etMouthOpening)
        val etToothNumber = findViewById<EditText>(R.id.etToothNumber)
        val etClinicalNotes = findViewById<EditText>(R.id.etClinicalNotes)
        val cbSwelling = findViewById<CheckBox>(R.id.cbSwelling)
        val cbInfection = findViewById<CheckBox>(R.id.cbInfection)

        // Spinners & Selections
        val spinnerImpaction = findViewById<RelativeLayout>(R.id.spinnerImpaction)
        val tvSelectedImpaction = findViewById<TextView>(R.id.tvSelectedImpaction)

        val spinnerPellGregory = findViewById<RelativeLayout>(R.id.spinnerPellGregory)
        val tvSelectedPellGregory = findViewById<TextView>(R.id.tvSelectedPellGregory)

        val spinnerWinter = findViewById<RelativeLayout>(R.id.spinnerWinter)
        val tvSelectedWinter = findViewById<TextView>(R.id.tvSelectedWinter)

        val spinnerUpperThird = findViewById<RelativeLayout>(R.id.spinnerUpperThird)
        val tvSelectedUpperThird = findViewById<TextView>(R.id.tvSelectedUpperThird)

        val spinnerDifficulty = findViewById<RelativeLayout>(R.id.spinnerDifficulty)
        val tvSelectedDifficulty = findViewById<TextView>(R.id.tvSelectedDifficulty)

        // Status Indicators
        val dotMouthOpening = findViewById<View>(R.id.dotMouthOpening)
        val tvMouthOpeningStatus = findViewById<TextView>(R.id.tvMouthOpeningStatus)

        // Expandable Why Dropdowns
        val btnWhyMouthOpening = findViewById<RelativeLayout>(R.id.btnWhyMouthOpening)
        val layoutWhyMouthOpening = findViewById<LinearLayout>(R.id.layoutWhyMouthOpening)
        val btnWhyImpaction = findViewById<RelativeLayout>(R.id.btnWhyImpaction)
        val layoutWhyImpaction = findViewById<LinearLayout>(R.id.layoutWhyImpaction)
        val btnWhyInfection = findViewById<RelativeLayout>(R.id.btnWhyInfection)
        val layoutWhyInfection = findViewById<LinearLayout>(R.id.layoutWhyInfection)
        val btnWhyDifficulty = findViewById<RelativeLayout>(R.id.btnWhyDifficulty)
        val layoutWhyDifficulty = findViewById<LinearLayout>(R.id.layoutWhyDifficulty)

        fun setupToggle(trigger: View, target: View) {
            trigger.setOnClickListener {
                target.visibility = if (target.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
        setupToggle(btnWhyMouthOpening, layoutWhyMouthOpening)
        setupToggle(btnWhyImpaction, layoutWhyImpaction)
        setupToggle(btnWhyInfection, layoutWhyInfection)
        setupToggle(btnWhyDifficulty, layoutWhyDifficulty)

        // Mouth Opening TextWatcher Check (Normal > 35mm)
        etMouthOpening.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toIntOrNull() ?: 40
                if (value >= 35) {
                    dotMouthOpening.backgroundTintList = ColorStateList.valueOf(getColor(R.color.status_green))
                    tvMouthOpeningStatus.text = "Normal"
                    tvMouthOpeningStatus.setTextColor(getColor(R.color.status_green))
                } else {
                    dotMouthOpening.backgroundTintList = ColorStateList.valueOf(getColor(R.color.status_red))
                    tvMouthOpeningStatus.text = "Abnormal"
                    tvMouthOpeningStatus.setTextColor(getColor(R.color.status_red))
                }
            }
        })

        // Spinner Dropdowns configuration
        val openImpactionMenu = {
            val popup = PopupMenu(this, spinnerImpaction)
            popup.menu.add("Soft Tissue Impaction")
            popup.menu.add("Partial Bony Impaction")
            popup.menu.add("Complete Bony Impaction")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedImpaction.text = item.title
                true
            }
            popup.show()
        }
        spinnerImpaction.setOnClickListener { openImpactionMenu() }
        tvSelectedImpaction.setOnClickListener { openImpactionMenu() }

        val openPellGregoryMenu = {
            val popup = PopupMenu(this, spinnerPellGregory)
            popup.menu.add("Class I, Position A")
            popup.menu.add("Class I, Position B")
            popup.menu.add("Class I, Position C")
            popup.menu.add("Class II, Position A")
            popup.menu.add("Class II, Position B")
            popup.menu.add("Class II, Position C")
            popup.menu.add("Class III, Position A")
            popup.menu.add("Class III, Position B")
            popup.menu.add("Class III, Position C")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedPellGregory.text = item.title
                true
            }
            popup.show()
        }
        spinnerPellGregory.setOnClickListener { openPellGregoryMenu() }
        tvSelectedPellGregory.setOnClickListener { openPellGregoryMenu() }

        val openWinterMenu = {
            val popup = PopupMenu(this, spinnerWinter)
            popup.menu.add("Mesioangular")
            popup.menu.add("Horizontal")
            popup.menu.add("Vertical")
            popup.menu.add("Distoangular")
            popup.menu.add("Buccoangular")
            popup.menu.add("Linguoangular")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedWinter.text = item.title
                true
            }
            popup.show()
        }
        spinnerWinter.setOnClickListener { openWinterMenu() }
        tvSelectedWinter.setOnClickListener { openWinterMenu() }

        val openUpperThirdMenu = {
            val popup = PopupMenu(this, spinnerUpperThird)
            popup.menu.add("Class A")
            popup.menu.add("Class B")
            popup.menu.add("Class C")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedUpperThird.text = item.title
                true
            }
            popup.show()
        }
        spinnerUpperThird.setOnClickListener { openUpperThirdMenu() }
        tvSelectedUpperThird.setOnClickListener { openUpperThirdMenu() }

        val openDifficultyMenu = {
            val popup = PopupMenu(this, spinnerDifficulty)
            popup.menu.add("Easy")
            popup.menu.add("Moderate")
            popup.menu.add("Difficult")
            popup.menu.add("Very Difficult")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedDifficulty.text = item.title
                true
            }
            popup.show()
        }
        spinnerDifficulty.setOnClickListener { openDifficultyMenu() }
        tvSelectedDifficulty.setOnClickListener { openDifficultyMenu() }

        // Actions
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)

        btnWizardBack.setOnClickListener {
            finish()
        }

        // Skip button
        val btnWizardSkip = findViewById<android.widget.TextView?>(R.id.btnWizardSkip)
        btnWizardSkip?.setOnClickListener {
            startActivity(Intent(this, OMFSWizardStep7Activity::class.java).apply {
                putExtra("patient_id", patientId)
                putExtra("patient_name", name)
                putExtra("patient_age", age)
                putExtra("patient_gender", gender)
                putExtra("patient_procedure", procedure)
                putExtra("patient_asa", asa)
                putStringArrayListExtra("patient_allergies", allergies)
            })
        }

        btnWizardNext.setOnClickListener {
            val opening = etMouthOpening.text.toString().trim()
            val tooth = etToothNumber.text.toString().trim()
            val notes = etClinicalNotes.text.toString().trim()

            if (opening.isEmpty() || tooth.isEmpty()) {
                Toast.makeText(this, "Please enter mouth opening and tooth number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val difficultyScore = when (tvSelectedDifficulty.text.toString()) {
                "Easy" -> 1
                "Moderate" -> 2
                "Difficult" -> 3
                "Very Difficult" -> 4
                else -> null
            }
            val mouthOpeningValue = opening.toIntOrNull()
            val request = DentalExaminationRequest(
                asaClass = "ASA $asa",
                pellGregoryClass = tvSelectedPellGregory.text.toString(),
                winterClassification = tvSelectedWinter.text.toString(),
                upperThirdMolar = tvSelectedUpperThird.text.toString(),
                difficultyScore = difficultyScore,
                mouthOpeningMm = mouthOpeningValue,
                activeInfection = cbInfection.isChecked,
                swelling = cbSwelling.isChecked,
                trismus = (mouthOpeningValue ?: 40) < 35,
                toothNumber = tooth,
                clinicalExaminationNotes = notes
            )

            val tvNextText = btnWizardNext.getChildAt(0) as? android.widget.TextView
            btnWizardNext.isEnabled = false
            tvNextText?.text = "Saving…"

            lifecycleScope.launch {
                wizardRepository.saveDental(patientId, request)
                    .onSuccess {
                        val intent = Intent(this@OMFSWizardStep6Activity, OMFSWizardStep7Activity::class.java).apply {
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
                putExtra("exam_mouth_opening", opening)
                putExtra("exam_tooth_number", tooth)
                putExtra("exam_impaction_type", tvSelectedImpaction.text.toString())
                putExtra("exam_pell_gregory", tvSelectedPellGregory.text.toString())
                putExtra("exam_winter", tvSelectedWinter.text.toString())
                putExtra("exam_upper_third", tvSelectedUpperThird.text.toString())
                putExtra("exam_swelling", cbSwelling.isChecked)
                putExtra("exam_infection", cbInfection.isChecked)
                putExtra("exam_difficulty", tvSelectedDifficulty.text.toString())
                putExtra("exam_notes", notes)
                        }
                        startActivity(intent)
                    }
                    .onFailure {
                        btnWizardNext.isEnabled = true
                        tvNextText?.text = "Next step →"
                        Toast.makeText(this@OMFSWizardStep6Activity, it.message ?: "Unable to save dental examination", Toast.LENGTH_LONG).show()
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
