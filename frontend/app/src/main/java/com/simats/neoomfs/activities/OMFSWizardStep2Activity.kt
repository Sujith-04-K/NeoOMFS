package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.models.PatientVitalsRequest
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.WizardRepository
import kotlinx.coroutines.launch

class OMFSWizardStep2Activity : AppCompatActivity() {
    private val wizardRepository = WizardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step2)

        RetrofitClient.initialize(applicationContext)

        // Retrieve data from previous activity
        val patientId = intent.getLongExtra("patient_id", -1L)
        val name = intent.getStringExtra("patient_name") ?: ""
        val age = intent.getStringExtra("patient_age") ?: ""
        val gender = intent.getStringExtra("patient_gender") ?: ""
        val procedure = intent.getStringExtra("patient_procedure") ?: ""
        val asa = intent.getIntExtra("patient_asa", 1)
        val allergies = intent.getStringArrayListExtra("patient_allergies") ?: arrayListOf()

        // Edit Texts
        val etSystolic = findViewById<EditText>(R.id.etSystolic)
        val etDiastolic = findViewById<EditText>(R.id.etDiastolic)
        val etPulseRate = findViewById<EditText>(R.id.etPulseRate)
        val etTemperature = findViewById<EditText>(R.id.etTemperature)
        val etRespiratory = findViewById<EditText>(R.id.etRespiratory)
        val etSpO2 = findViewById<EditText>(R.id.etSpO2)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)

        // Status Indicators
        val dotBp = findViewById<View>(R.id.dotBp)
        val tvBpStatus = findViewById<TextView>(R.id.tvBpStatus)

        val dotPulse = findViewById<View>(R.id.dotPulse)
        val tvPulseStatus = findViewById<TextView>(R.id.tvPulseStatus)

        val dotTemp = findViewById<View>(R.id.dotTemp)
        val tvTempStatus = findViewById<TextView>(R.id.tvTempStatus)

        val dotResp = findViewById<View>(R.id.dotResp)
        val tvRespStatus = findViewById<TextView>(R.id.tvRespStatus)

        val dotSpo2 = findViewById<View>(R.id.dotSpo2)
        val tvSpo2Status = findViewById<TextView>(R.id.tvSpo2Status)

        val dotBmi = findViewById<View>(R.id.dotBmi)
        val tvBmiStatus = findViewById<TextView>(R.id.tvBmiStatus)

        // TextWatcher helpers
        fun setVitalsCheck(editText: EditText, checkFunc: () -> Boolean, statusDot: View, statusText: TextView) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val isNormal = checkFunc()
                    if (isNormal) {
                        statusDot.backgroundTintList = getColorStateList(R.color.status_green)
                        statusText.text = "Normal"
                        statusText.setTextColor(getColor(R.color.status_green))
                    } else {
                        statusDot.backgroundTintList = getColorStateList(R.color.status_red)
                        statusText.text = "Abnormal"
                        statusText.setTextColor(getColor(R.color.status_red))
                    }
                }
            })
        }

        // 1. BP check (Systolic 90-120 and Diastolic 60-80)
        val bpCheck = {
            val sysVal = etSystolic.text.toString().toIntOrNull() ?: 120
            val diaVal = etDiastolic.text.toString().toIntOrNull() ?: 80
            sysVal in 90..120 && diaVal in 60..80
        }
        setVitalsCheck(etSystolic, bpCheck, dotBp, tvBpStatus)
        setVitalsCheck(etDiastolic, bpCheck, dotBp, tvBpStatus)

        // 2. Pulse check (60-100 BPM)
        setVitalsCheck(etPulseRate, {
            val pVal = etPulseRate.text.toString().toIntOrNull() ?: 72
            pVal in 60..100
        }, dotPulse, tvPulseStatus)

        // 3. Temp check (97.0 - 99.1 F)
        setVitalsCheck(etTemperature, {
            val tVal = etTemperature.text.toString().toFloatOrNull() ?: 98.6f
            tVal >= 97.0f && tVal <= 99.1f
        }, dotTemp, tvTempStatus)

        // 4. Resp check (12-20 BPM)
        setVitalsCheck(etRespiratory, {
            val rVal = etRespiratory.text.toString().toIntOrNull() ?: 16
            rVal in 12..20
        }, dotResp, tvRespStatus)

        // 5. SpO2 check (95-100 %)
        setVitalsCheck(etSpO2, {
            val sVal = etSpO2.text.toString().toIntOrNull() ?: 98
            sVal in 95..100
        }, dotSpo2, tvSpo2Status)

        // 6. Height/Weight (dummy simple normal check)
        val bmiCheck = {
            val hVal = etHeight.text.toString().toDoubleOrNull() ?: 170.0
            val wVal = etWeight.text.toString().toDoubleOrNull() ?: 70.0
            hVal > 100.0 && wVal > 30.0
        }
        setVitalsCheck(etHeight, bmiCheck, dotBmi, tvBmiStatus)
        setVitalsCheck(etWeight, bmiCheck, dotBmi, tvBmiStatus)

        // Action Buttons
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)

        btnWizardBack.setOnClickListener {
            // Finish this step, return to Step 1
            finish()
        }

        btnWizardNext.setOnClickListener {
            val systolic = etSystolic.text.toString().trim()
            val diastolic = etDiastolic.text.toString().trim()
            val pulse = etPulseRate.text.toString().trim()
            val temp = etTemperature.text.toString().trim()
            val respiratory = etRespiratory.text.toString().trim()
            val spo2 = etSpO2.text.toString().trim()

            if (systolic.isEmpty() || diastolic.isEmpty() || pulse.isEmpty() || temp.isEmpty() || respiratory.isEmpty() || spo2.isEmpty()) {
                Toast.makeText(this, "Please enter all vital parameters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = PatientVitalsRequest(
                bpSystolic = systolic.toIntOrNull(),
                bpDiastolic = diastolic.toIntOrNull(),
                temperature = temp.toDoubleOrNull(),
                pulseRate = pulse.toIntOrNull(),
                spo2 = spo2.toDoubleOrNull(),
                respiratoryRate = respiratory.toIntOrNull(),
                heightCm = etHeight.text.toString().trim().toDoubleOrNull(),
                weightKg = etWeight.text.toString().trim().toDoubleOrNull(),
                bmi = null
            )

            lifecycleScope.launch {
                wizardRepository.saveVitals(patientId, request)
                    .onSuccess {
                        val intent = Intent(this@OMFSWizardStep2Activity, OMFSWizardStep3Activity::class.java).apply {
                            putExtra("patient_id", patientId)
                            putExtra("patient_name", name)
                            putExtra("patient_age", age)
                            putExtra("patient_gender", gender)
                            putExtra("patient_procedure", procedure)
                            putExtra("patient_asa", asa)
                            putStringArrayListExtra("patient_allergies", allergies)
                            putExtra("vital_bp_sys", systolic)
                            putExtra("vital_bp_dia", diastolic)
                            putExtra("vital_pulse", pulse)
                            putExtra("vital_temp", temp)
                            putExtra("vital_resp", respiratory)
                            putExtra("vital_spo2", spo2)
                        }
                        startActivity(intent)
                    }
                    .onFailure {
                        Toast.makeText(this@OMFSWizardStep2Activity, it.message ?: "Unable to save vitals", Toast.LENGTH_LONG).show()
                    }
            }
        }

        // Bottom Navigation bindings
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<LinearLayout>(R.id.btnNavAssess)
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
}
