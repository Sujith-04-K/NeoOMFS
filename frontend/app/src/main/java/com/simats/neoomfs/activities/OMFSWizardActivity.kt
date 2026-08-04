package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation

class OMFSWizardActivity : AppCompatActivity() {

    private var currentStep = 1
    private val selectedAllergies = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard)

        // Retrieve data from intent
        val patientId = intent.getLongExtra("patient_id", -1L)
        val name = intent.getStringExtra("patient_name") ?: "John Mathew"
        val age = intent.getStringExtra("patient_age") ?: "27"
        val gender = intent.getStringExtra("patient_gender") ?: "Other"
        val procedure = intent.getStringExtra("patient_procedure") ?: "Third Molar Extraction"
        val asa = intent.getIntExtra("patient_asa", 1)
        val allergies = intent.getStringArrayListExtra("patient_allergies") ?: arrayListOf("Penicillin", "Latex")

        selectedAllergies.addAll(allergies)

        // Find views
        val tvStepBadge = findViewById<TextView>(R.id.tvStepBadge)
        val wizardProgressBar = findViewById<ProgressBar>(R.id.wizardProgressBar)

        // Steps layouts
        val layoutStep1 = findViewById<LinearLayout>(R.id.layoutStep1)

        // Step 1 Profile Details
        val tvWizardProfileName = findViewById<TextView>(R.id.tvWizardProfileName)
        val tvWizardProfileAge = findViewById<TextView>(R.id.tvWizardProfileAge)
        val tvWizardProfileGender = findViewById<TextView>(R.id.tvWizardProfileGender)
        val tvWizardSelectedProcedure = findViewById<TextView>(R.id.tvWizardSelectedProcedure)
        val tvWizardSelectedAsa = findViewById<TextView>(R.id.tvWizardSelectedAsa)
        val spinnerWizardProcedure = findViewById<RelativeLayout>(R.id.spinnerWizardProcedure)
        val spinnerWizardAsa = findViewById<RelativeLayout>(R.id.spinnerWizardAsa)

        // Why dropdown toggles
        fun setupToggle(button: View, target: View) {
            button.setOnClickListener {
                if (target.visibility == View.VISIBLE) {
                    target.visibility = View.GONE
                } else {
                    target.visibility = View.VISIBLE
                }
            }
        }

        setupToggle(findViewById(R.id.btnWhyChiefComplaint), findViewById(R.id.layoutWhyChiefComplaint))
        setupToggle(findViewById(R.id.btnWhyProcedure), findViewById(R.id.layoutWhyProcedure))
        setupToggle(findViewById(R.id.btnWhyAsa), findViewById(R.id.layoutWhyAsa))
        setupToggle(findViewById(R.id.btnWhyAllergies), findViewById(R.id.layoutWhyAllergies))

        // Step 1 Allergies
        val layoutWizardAllergyChips = findViewById<LinearLayout>(R.id.layoutWizardAllergyChips)
        val chipWizardPenicillin = findViewById<LinearLayout>(R.id.chipWizardPenicillin)
        val chipWizardLatex = findViewById<LinearLayout>(R.id.chipWizardLatex)
        val btnRemoveWizardPenicillin = findViewById<TextView>(R.id.btnRemoveWizardPenicillin)
        val btnRemoveWizardLatex = findViewById<TextView>(R.id.btnRemoveWizardLatex)

        val etWizardNewAllergy = findViewById<EditText>(R.id.etWizardNewAllergy)
        val btnWizardAddAllergy = findViewById<LinearLayout>(R.id.btnWizardAddAllergy)

        // Action controllers
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val tvWizardBackText = findViewById<TextView>(R.id.tvWizardBackText)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)
        val tvWizardNextText = findViewById<TextView>(R.id.tvWizardNextText)

        // Bottom nav tabs
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<LinearLayout>(R.id.btnNavAssess)
        val btnNavSettings = findViewById<FrameLayout>(R.id.btnNavSettings)

        // Populate initial profile metrics
        tvWizardProfileName.text = name
        tvWizardProfileAge.text = "$age yrs"
        tvWizardProfileGender.text = gender
        tvWizardSelectedProcedure.text = procedure
        tvWizardSelectedAsa.text = when(asa) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            else -> "I"
        }

        // Clean up static allergy chips based on list
        if (!selectedAllergies.contains("Penicillin")) {
            layoutWizardAllergyChips.removeView(chipWizardPenicillin)
        }
        if (!selectedAllergies.contains("Latex")) {
            layoutWizardAllergyChips.removeView(chipWizardLatex)
        }

        btnRemoveWizardPenicillin.setOnClickListener {
            layoutWizardAllergyChips.removeView(chipWizardPenicillin)
            selectedAllergies.remove("Penicillin")
        }

        btnRemoveWizardLatex.setOnClickListener {
            layoutWizardAllergyChips.removeView(chipWizardLatex)
            selectedAllergies.remove("Latex")
        }

        // 1. Dynamic chip adder in wizard summary
        btnWizardAddAllergy.setOnClickListener {
            val allergy = etWizardNewAllergy.text.toString().trim()
            if (allergy.isEmpty()) {
                Toast.makeText(this, "Enter allergy name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedAllergies.contains(allergy)) {
                Toast.makeText(this, "Allergy already added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val density = resources.displayMetrics.density
            val newChip = LinearLayout(this)
            newChip.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (32 * density).toInt()
            ).apply {
                marginEnd = (8 * density).toInt()
            }
            newChip.background = getDrawable(R.drawable.bg_chip_allergy)
            newChip.orientation = LinearLayout.HORIZONTAL
            newChip.gravity = Gravity.CENTER_VERTICAL
            newChip.setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)

            val tvName = TextView(this)
            tvName.text = allergy
            tvName.setTextColor(getColor(R.color.status_red))
            tvName.textSize = 12f
            tvName.typeface = Typeface.DEFAULT_BOLD

            val tvClose = TextView(this)
            tvClose.text = " ✕"
            tvClose.setTextColor(getColor(R.color.status_red))
            tvClose.textSize = 12f
            tvClose.setPadding((6 * density).toInt(), 0, 0, 0)
            tvClose.setOnClickListener {
                layoutWizardAllergyChips.removeView(newChip)
                selectedAllergies.remove(allergy)
            }

            newChip.addView(tvName)
            newChip.addView(tvClose)
            layoutWizardAllergyChips.addView(newChip, 0)
            selectedAllergies.add(allergy)

            etWizardNewAllergy.text.clear()
        }

        // 2. Click procedures spinners in wizard
        val openProcedureMenu = {
            val popup = PopupMenu(this, spinnerWizardProcedure)
            popup.menu.add("Third Molar Extraction")
            popup.menu.add("Implant Placement")
            popup.menu.add("Orthognathic Surgery")
            popup.menu.add("Biopsy")
            popup.menu.add("Cyst Enucleation")
            popup.setOnMenuItemClickListener { item ->
                tvWizardSelectedProcedure.text = item.title
                true
            }
            popup.show()
        }
        spinnerWizardProcedure.setOnClickListener { openProcedureMenu() }
        tvWizardSelectedProcedure.setOnClickListener { openProcedureMenu() }

        val openAsaMenu = {
            val popup = PopupMenu(this, spinnerWizardAsa)
            popup.menu.add("I - Normal Healthy")
            popup.menu.add("II - Mild Systemic")
            popup.menu.add("III - Severe Systemic")
            popup.menu.add("IV - Life Threatening")
            popup.menu.add("V - Moribund")
            popup.menu.add("VI - Brain-Dead")
            popup.setOnMenuItemClickListener { item ->
                tvWizardSelectedAsa.text = item.title?.toString()?.split(" ")?.getOrNull(0) ?: "I"
                true
            }
            popup.show()
        }
        spinnerWizardAsa.setOnClickListener { openAsaMenu() }
        tvWizardSelectedAsa.setOnClickListener { openAsaMenu() }


        btnWizardBack.setOnClickListener {
            finish()
        }

        btnWizardNext.setOnClickListener {
            val intent = Intent(this, OMFSWizardStep2Activity::class.java).apply {
                putExtra("patient_id", patientId)
                putExtra("patient_name", name)
                putExtra("patient_age", age)
                putExtra("patient_gender", tvWizardProfileGender.text.toString())
                putExtra("patient_procedure", tvWizardSelectedProcedure.text.toString())
                putExtra("patient_asa", when (tvWizardSelectedAsa.text.toString().trim()) {
                    "I" -> 1
                    "II" -> 2
                    "III" -> 3
                    "IV" -> 4
                    "V" -> 5
                    "VI" -> 6
                    else -> 1
                })
                putStringArrayListExtra("patient_allergies", ArrayList(selectedAllergies))
            }
            startActivity(intent)
        }

        // Bottom Navigation bindings
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
