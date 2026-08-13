package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.models.LaboratoryRequest
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.WizardRepository
import kotlinx.coroutines.launch

class OMFSWizardStep4Activity : AppCompatActivity() {
    private val wizardRepository = WizardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step4)

        RetrofitClient.initialize(applicationContext)

        // Retrieve bundle data
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

        // Form Inputs
        val etBloodGroup = findViewById<EditText>(R.id.etBloodGroup)
        val etRbs = findViewById<EditText>(R.id.etRbs)
        val etFbs = findViewById<EditText>(R.id.etFbs)
        val etBt = findViewById<EditText>(R.id.etBt)
        val etCt = findViewById<EditText>(R.id.etCt)
        val etHb = findViewById<EditText>(R.id.etHb)
        val etWbc = findViewById<EditText>(R.id.etWbc)
        val etPlatelets = findViewById<EditText>(R.id.etPlatelets)
        val etPt = findViewById<EditText>(R.id.etPt)
        val etInr = findViewById<EditText>(R.id.etInr)

        // Status Indicators
        val tvBloodGroupStatus = findViewById<TextView>(R.id.tvBloodGroupStatus)
        val dotSugar = findViewById<View>(R.id.dotSugar)
        val tvSugarStatus = findViewById<TextView>(R.id.tvSugarStatus)
        val dotBtCt = findViewById<View>(R.id.dotBtCt)
        val tvBtCtStatus = findViewById<TextView>(R.id.tvBtCtStatus)
        val dotCbc = findViewById<View>(R.id.dotCbc)
        val tvCbcStatus = findViewById<TextView>(R.id.tvCbcStatus)
        val dotPtInr = findViewById<View>(R.id.dotPtInr)
        val tvPtInrStatus = findViewById<TextView>(R.id.tvPtInrStatus)

        // Expandable Why Layouts
        val btnWhyBloodGroup = findViewById<RelativeLayout>(R.id.btnWhyBloodGroup)
        val layoutWhyBloodGroup = findViewById<LinearLayout>(R.id.layoutWhyBloodGroup)
        val btnWhySugar = findViewById<RelativeLayout>(R.id.btnWhySugar)
        val layoutWhySugar = findViewById<LinearLayout>(R.id.layoutWhySugar)
        val btnWhyBtCt = findViewById<RelativeLayout>(R.id.btnWhyBtCt)
        val layoutWhyBtCt = findViewById<LinearLayout>(R.id.layoutWhyBtCt)
        val btnWhyCbc = findViewById<RelativeLayout>(R.id.btnWhyCbc)
        val layoutWhyCbc = findViewById<LinearLayout>(R.id.layoutWhyCbc)
        val btnWhyPtInr = findViewById<RelativeLayout>(R.id.btnWhyPtInr)
        val layoutWhyPtInr = findViewById<LinearLayout>(R.id.layoutWhyPtInr)

        // Setup dropdown triggers
        fun setupToggle(trigger: View, target: View) {
            trigger.setOnClickListener {
                if (target.visibility == View.VISIBLE) {
                    target.visibility = View.GONE
                } else {
                    target.visibility = View.VISIBLE
                }
            }
        }

        setupToggle(btnWhyBloodGroup, layoutWhyBloodGroup)
        setupToggle(btnWhySugar, layoutWhySugar)
        setupToggle(btnWhyBtCt, layoutWhyBtCt)
        setupToggle(btnWhyCbc, layoutWhyCbc)
        setupToggle(btnWhyPtInr, layoutWhyPtInr)

        // Textwatchers for parameters checks
        etBloodGroup.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val group = s.toString().trim()
                if (group.isNotEmpty()) {
                    tvBloodGroupStatus.text = group.uppercase()
                    tvBloodGroupStatus.setTextColor(getColor(R.color.accent_blue))
                } else {
                    tvBloodGroupStatus.text = "Not selected"
                    tvBloodGroupStatus.setTextColor(getColor(R.color.text_secondary_gray))
                }
            }
        })

        fun setVitalsWatcher(editTexts: List<EditText>, checkFunc: () -> Boolean, dot: View, text: TextView) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val isNormal = checkFunc()
                    if (isNormal) {
                        dot.backgroundTintList = ColorStateList.valueOf(getColor(R.color.status_green))
                        text.text = "Normal"
                        text.setTextColor(getColor(R.color.status_green))
                    } else {
                        dot.backgroundTintList = ColorStateList.valueOf(getColor(R.color.status_red))
                        text.text = "Abnormal"
                        text.setTextColor(getColor(R.color.status_red))
                    }
                }
            }
            editTexts.forEach { it.addTextChangedListener(watcher) }
        }

        // Blood sugar check: RBS (70-140) and FBS (70-100)
        setVitalsWatcher(listOf(etRbs, etFbs), {
            val rbs = etRbs.text.toString().toIntOrNull() ?: 100
            val fbs = etFbs.text.toString().toIntOrNull() ?: 85
            rbs in 70..140 && fbs in 70..100
        }, dotSugar, tvSugarStatus)

        // BT & CT check: BT (2.0-7.0) and CT (5.0-15.0)
        setVitalsWatcher(listOf(etBt, etCt), {
            val bt = etBt.text.toString().toFloatOrNull() ?: 4.0f
            val ct = etCt.text.toString().toFloatOrNull() ?: 8.0f
            bt in 2.0f..7.0f && ct in 5.0f..15.0f
        }, dotBtCt, tvBtCtStatus)

        // CBC: Hb (12.0-17.5), WBC (4000-11000), Platelets (150000-450000)
        setVitalsWatcher(listOf(etHb, etWbc, etPlatelets), {
            val hb = etHb.text.toString().toFloatOrNull() ?: 14.0f
            val wbc = etWbc.text.toString().toIntOrNull() ?: 7000
            val platelets = etPlatelets.text.toString().toIntOrNull() ?: 250000
            hb in 12.0f..17.5f && wbc in 4000..11000 && platelets in 150000..450000
        }, dotCbc, tvCbcStatus)

        // PT / INR: PT (11.0-13.5), INR (0.8-1.2)
        setVitalsWatcher(listOf(etPt, etInr), {
            val pt = etPt.text.toString().toFloatOrNull() ?: 12.0f
            val inr = etInr.text.toString().toFloatOrNull() ?: 1.0f
            pt in 11.0f..13.5f && inr in 0.8f..1.2f
        }, dotPtInr, tvPtInrStatus)

        // Actions
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)

        btnWizardBack.setOnClickListener {
            finish()
        }

        // Skip button
        val btnWizardSkip = findViewById<android.widget.TextView?>(R.id.btnWizardSkip)
        btnWizardSkip?.setOnClickListener {
            startActivity(Intent(this, OMFSWizardStep5Activity::class.java).apply {
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
            val bg = etBloodGroup.text.toString().trim()
            val rbs = etRbs.text.toString().trim()
            val fbs = etFbs.text.toString().trim()
            val bt = etBt.text.toString().trim()
            val ct = etCt.text.toString().trim()
            val hb = etHb.text.toString().trim()
            val wbc = etWbc.text.toString().trim()
            val platelets = etPlatelets.text.toString().trim()
            val pt = etPt.text.toString().trim()
            val inr = etInr.text.toString().trim()

            if (bg.isEmpty() || rbs.isEmpty() || fbs.isEmpty() || bt.isEmpty() || ct.isEmpty() ||
                hb.isEmpty() || wbc.isEmpty() || platelets.isEmpty() || pt.isEmpty() || inr.isEmpty()) {
                Toast.makeText(this, "Please enter all lab parameters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LaboratoryRequest(
                hemoglobin = hb.toDoubleOrNull(),
                totalWbcCount = wbc.toIntOrNull(),
                plateletCount = platelets.toIntOrNull(),
                bleedingTime = bt.toDoubleOrNull(),
                clottingTime = ct.toDoubleOrNull(),
                pt = pt.toDoubleOrNull(),
                inr = inr.toDoubleOrNull(),
                fastingBloodSugar = fbs.toDoubleOrNull(),
                randomBloodSugar = rbs.toDoubleOrNull(),
                bloodGroup = bg
            )

            val tvNextText = btnWizardNext.getChildAt(0) as? android.widget.TextView
            btnWizardNext.isEnabled = false
            tvNextText?.text = "Saving…"

            lifecycleScope.launch {
                wizardRepository.saveLaboratory(patientId, request)
                    .onSuccess {
                        val intent = Intent(this@OMFSWizardStep4Activity, OMFSWizardStep5Activity::class.java).apply {
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
                putExtra("lab_blood_group", bg)
                putExtra("lab_rbs", rbs)
                putExtra("lab_fbs", fbs)
                putExtra("lab_bt", bt)
                putExtra("lab_ct", ct)
                putExtra("lab_hb", hb)
                putExtra("lab_wbc", wbc)
                putExtra("lab_platelets", platelets)
                putExtra("lab_pt", pt)
                putExtra("lab_inr", inr)
                        }
                        startActivity(intent)
                    }
                    .onFailure {
                        btnWizardNext.isEnabled = true
                        tvNextText?.text = "Next step →"
                        Toast.makeText(this@OMFSWizardStep4Activity, it.message ?: "Unable to save laboratory data", Toast.LENGTH_LONG).show()
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
