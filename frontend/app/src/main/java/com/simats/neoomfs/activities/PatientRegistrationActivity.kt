package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.models.PatientRequest
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.AuthRepository
import com.simats.neoomfs.repository.BackendPatientRepository
import kotlinx.coroutines.launch

class PatientRegistrationActivity : AppCompatActivity() {

    private val patientRepository = BackendPatientRepository()
    private lateinit var authRepository: AuthRepository
    private var selectedAsa = 1
    private val selectedAllergies = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)

        RetrofitClient.initialize(applicationContext)
        authRepository = AuthRepository(applicationContext)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etMedicalHistory = findViewById<EditText>(R.id.etMedicalHistory)

        // Spinners
        val spinnerGender = findViewById<RelativeLayout>(R.id.spinnerGender)
        val tvSelectedGender = findViewById<TextView>(R.id.tvSelectedGender)
        val spinnerProcedure = findViewById<RelativeLayout>(R.id.spinnerProcedure)
        val tvSelectedProcedure = findViewById<TextView>(R.id.tvSelectedProcedure)

        // ASA Grid Layout elements
        val btnAsa1 = findViewById<LinearLayout>(R.id.btnAsa1)
        val btnAsa2 = findViewById<LinearLayout>(R.id.btnAsa2)
        val btnAsa3 = findViewById<LinearLayout>(R.id.btnAsa3)
        val btnAsa4 = findViewById<LinearLayout>(R.id.btnAsa4)
        val btnAsa5 = findViewById<LinearLayout>(R.id.btnAsa5)
        val btnAsa6 = findViewById<LinearLayout>(R.id.btnAsa6)

        val asaButtons = listOf(btnAsa1, btnAsa2, btnAsa3, btnAsa4, btnAsa5, btnAsa6)

        // Allergies
        val layoutAllergyChips = findViewById<LinearLayout>(R.id.layoutAllergyChips)
        val chipPenicillin = findViewById<LinearLayout>(R.id.chipPenicillin)
        val chipLatex = findViewById<LinearLayout>(R.id.chipLatex)
        val btnRemovePenicillin = findViewById<TextView>(R.id.btnRemovePenicillin)
        val btnRemoveLatex = findViewById<TextView>(R.id.btnRemoveLatex)

        // Hide pre-seeded allergy chips – user must add allergies explicitly
        chipPenicillin.visibility = android.view.View.GONE
        chipLatex.visibility = android.view.View.GONE

        val etNewAllergy = findViewById<EditText>(R.id.etNewAllergy)
        val btnAddAllergy = findViewById<LinearLayout>(R.id.btnAddAllergy)

        // Action Buttons
        val btnSaveDraft = findViewById<LinearLayout>(R.id.btnSaveDraft)
        val btnContinueToHistory = findViewById<LinearLayout>(R.id.btnContinueToHistory)

        // Bottom Nav Bar
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<LinearLayout>(R.id.btnNavAssess)
        val btnNavSettings = findViewById<FrameLayout>(R.id.btnNavSettings)

        // 1. Gender Spinner Popup
        val openGenderMenu = {
            val popup = PopupMenu(this, spinnerGender)
            popup.menu.add("Male")
            popup.menu.add("Female")
            popup.menu.add("Other")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedGender.text = item.title
                true
            }
            popup.show()
        }
        spinnerGender.setOnClickListener { openGenderMenu() }
        tvSelectedGender.setOnClickListener { openGenderMenu() }

        // 2. Procedure Spinner Popup
        val openProcedureMenu = {
            val popup = PopupMenu(this, spinnerProcedure)
            popup.menu.add("Third Molar Extraction")
            popup.menu.add("Implant Placement")
            popup.menu.add("Orthognathic Surgery")
            popup.menu.add("Biopsy")
            popup.menu.add("Cyst Enucleation")
            popup.setOnMenuItemClickListener { item ->
                tvSelectedProcedure.text = item.title
                true
            }
            popup.show()
        }
        spinnerProcedure.setOnClickListener { openProcedureMenu() }
        tvSelectedProcedure.setOnClickListener { openProcedureMenu() }

        // 3. ASA Highlight selection handler
        fun updateAsaSelection(selection: Int) {
            selectedAsa = selection
            val density = resources.displayMetrics.density

            for (i in 0 until asaButtons.size) {
                val button = asaButtons[i]
                val tvTitle = button.getChildAt(0) as TextView
                val tvSub = button.getChildAt(1) as TextView

                if (i == selection - 1) {
                    button.background = getDrawable(R.drawable.bg_card_active_blue)
                    tvTitle.setTextColor(getColor(R.color.accent_blue))
                    tvTitle.setTypeface(null, Typeface.BOLD)
                    tvSub.setTextColor(getColor(R.color.accent_blue))
                    tvSub.setTypeface(null, Typeface.BOLD)
                } else {
                    button.background = getDrawable(R.drawable.bg_card_white)
                    tvTitle.setTextColor(getColor(R.color.bg_dark_blue))
                    tvTitle.setTypeface(null, Typeface.BOLD)
                    tvSub.setTextColor(getColor(R.color.text_secondary_gray))
                    tvSub.setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        btnAsa1.setOnClickListener { updateAsaSelection(1) }
        btnAsa2.setOnClickListener { updateAsaSelection(2) }
        btnAsa3.setOnClickListener { updateAsaSelection(3) }
        btnAsa4.setOnClickListener { updateAsaSelection(4) }
        btnAsa5.setOnClickListener { updateAsaSelection(5) }
        btnAsa6.setOnClickListener { updateAsaSelection(6) }

        // 4. Allergy tags dismiss handlers
        btnRemovePenicillin.setOnClickListener {
            layoutAllergyChips.removeView(chipPenicillin)
            selectedAllergies.remove("Penicillin")
        }

        btnRemoveLatex.setOnClickListener {
            layoutAllergyChips.removeView(chipLatex)
            selectedAllergies.remove("Latex")
        }

        // 5. Dynamic Allergy Chips adder
        btnAddAllergy.setOnClickListener {
            val allergyText = etNewAllergy.text.toString().trim()
            if (allergyText.isEmpty()) {
                Toast.makeText(this, "Please enter an allergy label", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAllergies.contains(allergyText)) {
                Toast.makeText(this, "Allergy already added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create tag layout
            val density = resources.displayMetrics.density
            val newChip = LinearLayout(this)
            newChip.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (32 * density).toInt()
            ).apply {
                marginEnd = (8 * density).toInt()
                bottomMargin = (8 * density).toInt()
            }
            newChip.background = getDrawable(R.drawable.bg_chip_allergy)
            newChip.orientation = LinearLayout.HORIZONTAL
            newChip.gravity = Gravity.CENTER_VERTICAL
            newChip.setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)

            // Name
            val tvName = TextView(this)
            tvName.text = allergyText
            tvName.setTextColor(getColor(R.color.status_red))
            tvName.textSize = 12f
            tvName.typeface = Typeface.DEFAULT_BOLD

            // Close 'X' symbol
            val tvClose = TextView(this)
            tvClose.text = " ✕"
            tvClose.setTextColor(getColor(R.color.status_red))
            tvClose.textSize = 12f
            tvClose.setPadding((6 * density).toInt(), 0, 0, 0)
            tvClose.setOnClickListener {
                layoutAllergyChips.removeView(newChip)
                selectedAllergies.remove(allergyText)
            }

            newChip.addView(tvName)
            newChip.addView(tvClose)
            
            // Add view to container
            layoutAllergyChips.addView(newChip, 0)
            selectedAllergies.add(allergyText)
            
            etNewAllergy.text.clear()
        }

        // 6. Action buttons
        btnSaveDraft.setOnClickListener {
            savePatientRecord(
                etFullName.text.toString().trim(),
                etAge.text.toString().trim(),
                tvSelectedGender.text.toString(),
                etMobile.text.toString().trim(),
                etAddress.text.toString().trim(),
                tvSelectedProcedure.text.toString(),
                etMedicalHistory.text.toString().trim(),
                isDraft = true
            )
        }

        btnContinueToHistory.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val age = etAge.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter patient full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (age.isEmpty()) {
                Toast.makeText(this, "Please enter patient age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsedAge = age.toIntOrNull()
            if (parsedAge == null) {
                Toast.makeText(this, "Please enter a valid patient age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = PatientRequest(
                fullName = name,
                age = parsedAge,
                gender = tvSelectedGender.text.toString(),
                phoneNumber = etMobile.text.toString().trim().ifBlank { null },
                address = etAddress.text.toString().trim().ifBlank { null },
                procedureType = tvSelectedProcedure.text.toString().ifBlank { null },
                referringDoctor = authRepository.getStoredUser()?.fullName
            )

            lifecycleScope.launch {
                patientRepository.createPatient(request)
                    .onSuccess { patient ->
                        val intent = Intent(this@PatientRegistrationActivity, OMFSWizardActivity::class.java).apply {
                            putExtra("patient_id", patient.id)
                            putExtra("patient_name", name)
                            putExtra("patient_age", age)
                            putExtra("patient_gender", tvSelectedGender.text.toString())
                            putExtra("patient_procedure", tvSelectedProcedure.text.toString())
                            putExtra("patient_asa", selectedAsa)
                            putStringArrayListExtra("patient_allergies", ArrayList(selectedAllergies))
                        }
                        startActivity(intent)
                    }
                    .onFailure {
                        Toast.makeText(this@PatientRegistrationActivity, it.message ?: "Unable to create patient", Toast.LENGTH_LONG).show()
                    }
            }
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

    private fun savePatientRecord(
        name: String,
        age: String,
        gender: String,
        mobile: String,
        address: String,
        procedure: String,
        medicalHistory: String,
        isDraft: Boolean
    ) {
        if (!authRepository.isLoggedIn()) {
            Toast.makeText(this, "Please sign in to save patient records", Toast.LENGTH_LONG).show()
            return
        }

        if (name.isBlank()) {
            Toast.makeText(this, "Please enter patient full name", Toast.LENGTH_SHORT).show()
            return
        }

        val parsedAge = age.toIntOrNull()
        if (parsedAge == null) {
            Toast.makeText(this, "Please enter a valid patient age", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PatientRequest(
            fullName = name,
            age = parsedAge,
            gender = gender,
            phoneNumber = mobile.ifBlank { null },
            address = address.ifBlank { null },
            procedureType = procedure.ifBlank { null },
            referringDoctor = authRepository.getStoredUser()?.fullName
        )

        lifecycleScope.launch {
            patientRepository.createPatient(request)
                .onSuccess {
                    val message = if (isDraft) "Patient saved to backend" else "Patient created successfully"
                    Toast.makeText(this@PatientRegistrationActivity, message, Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(this@PatientRegistrationActivity, it.message ?: "Unable to save patient", Toast.LENGTH_LONG).show()
                }
        }
    }
}
