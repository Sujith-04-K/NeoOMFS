package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation
import androidx.lifecycle.lifecycleScope
import com.simats.neoomfs.models.RadiologyRequest
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.repository.AuthRepository
import com.simats.neoomfs.repository.BackendFileRepository
import com.simats.neoomfs.repository.BackendRadiologyRepository
import kotlinx.coroutines.launch

class OMFSWizardStep3Activity : AppCompatActivity() {

    private val fileRepository = BackendFileRepository()
    private val radiologyRepository = BackendRadiologyRepository()
    private lateinit var authRepository: AuthRepository
    private var activeUploadTarget = ""
    private var currentPatientId: Long? = null
    private var iopaUploaded = false
    private var opgUploaded = false
    private var cbctUploaded = false
    private var pendingUiUpdate: ((String, String) -> Unit)? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        uploadSelectedFile(uri)
    }

    private val cameraPicker = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: android.graphics.Bitmap? ->
        if (bitmap == null) {
            Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        val uri = saveBitmapToTempUri(bitmap)
        if (uri != null) {
            uploadSelectedFile(uri)
        } else {
            Toast.makeText(this, "Failed to save captured photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToTempUri(bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val file = java.io.File(cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            val output = java.io.FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, output)
            output.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omfs_wizard_step3)

        RetrofitClient.initialize(applicationContext)
        authRepository = AuthRepository(applicationContext)

        // Retrieve data from previous activity
        val name = intent.getStringExtra("patient_name") ?: ""
        val age = intent.getStringExtra("patient_age") ?: ""
        val gender = intent.getStringExtra("patient_gender") ?: ""
        val procedure = intent.getStringExtra("patient_procedure") ?: ""
        val asa = intent.getIntExtra("patient_asa", 1)
        val allergies = intent.getStringArrayListExtra("patient_allergies") ?: arrayListOf()

        // Find elements
        val cbIopa = findViewById<CheckBox>(R.id.cbIopa)
        val cbOpg = findViewById<CheckBox>(R.id.cbOpg)
        val cbCbct = findViewById<CheckBox>(R.id.cbCbct)

        val containerUploadIopa = findViewById<LinearLayout>(R.id.containerUploadIopa)
        val containerUploadOpg = findViewById<LinearLayout>(R.id.containerUploadOpg)
        val containerUploadCbct = findViewById<LinearLayout>(R.id.containerUploadCbct)

        val tvUploadIopaTitle = findViewById<TextView>(R.id.tvUploadIopaTitle)
        val tvUploadIopaSub = findViewById<TextView>(R.id.tvUploadIopaSub)

        val tvUploadOpgTitle = findViewById<TextView>(R.id.tvUploadOpgTitle)
        val tvUploadOpgSub = findViewById<TextView>(R.id.tvUploadOpgSub)

        val tvUploadCbctTitle = findViewById<TextView>(R.id.tvUploadCbctTitle)
        val tvUploadCbctSub = findViewById<TextView>(R.id.tvUploadCbctSub)

        // Popups
        val dimOverlay = findViewById<View>(R.id.dimOverlay)
        val layoutUploadDialog = findViewById<LinearLayout>(R.id.layoutUploadDialog)
        val layoutPhotoPicker = findViewById<LinearLayout>(R.id.layoutPhotoPicker)

        val btnDialogCamera = findViewById<TextView>(R.id.btnDialogCamera)
        val btnDialogDevice = findViewById<TextView>(R.id.btnDialogDevice)
        val btnDismissWarning = findViewById<TextView>(R.id.btnDismissWarning)

        // Mock Thumbs
        val thumb1 = findViewById<FrameLayout>(R.id.thumb1)
        val thumb2 = findViewById<FrameLayout>(R.id.thumb2)
        val thumb3 = findViewById<FrameLayout>(R.id.thumb3)
        val thumb4 = findViewById<FrameLayout>(R.id.thumb4)
        val thumb5 = findViewById<FrameLayout>(R.id.thumb5)
        val thumb6 = findViewById<FrameLayout>(R.id.thumb6)

        // Checkbox listeners
        cbIopa.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                containerUploadIopa.alpha = 1.0f
                containerUploadIopa.isEnabled = true
            } else {
                containerUploadIopa.alpha = 0.5f
                containerUploadIopa.isEnabled = false
                iopaUploaded = false
                tvUploadIopaTitle.text = "📥 Click to Upload File"
                tvUploadIopaSub.text = "Supports DICOM, PNG, OPG, IOPA scans"
                tvUploadIopaTitle.setTextColor(getColor(R.color.accent_blue))
                tvUploadIopaSub.setTextColor(getColor(R.color.text_secondary_gray))
            }
        }

        cbOpg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                containerUploadOpg.alpha = 1.0f
                containerUploadOpg.isEnabled = true
            } else {
                containerUploadOpg.alpha = 0.5f
                containerUploadOpg.isEnabled = false
                opgUploaded = false
                tvUploadOpgTitle.text = "📥 Click to Upload File"
                tvUploadOpgSub.text = "Supports DICOM, PNG, OPG, IOPA scans"
                tvUploadOpgTitle.setTextColor(getColor(R.color.accent_blue))
                tvUploadOpgSub.setTextColor(getColor(R.color.text_secondary_gray))
            }
        }

        cbCbct.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                containerUploadCbct.alpha = 1.0f
                containerUploadCbct.isEnabled = true
            } else {
                containerUploadCbct.alpha = 0.5f
                containerUploadCbct.isEnabled = false
                cbctUploaded = false
                tvUploadCbctTitle.text = "📥 Click to Upload File"
                tvUploadCbctSub.text = "Supports DICOM, PNG, OPG, IOPA scans"
                tvUploadCbctTitle.setTextColor(getColor(R.color.accent_blue))
                tvUploadCbctSub.setTextColor(getColor(R.color.text_secondary_gray))
            }
        }

        currentPatientId = intent.getLongExtra("patient_id", -1L).takeIf { it > 0L }

        // Show Dialog helper
        fun openUploadDialog(target: String) {
            activeUploadTarget = target
            dimOverlay.visibility = View.VISIBLE
            layoutUploadDialog.visibility = View.VISIBLE
        }

        val handleIopaClick = {
            if (cbIopa.isChecked) openUploadDialog("iopa")
        }
        containerUploadIopa.setOnClickListener { handleIopaClick() }
        tvUploadIopaTitle.setOnClickListener { handleIopaClick() }
        tvUploadIopaSub.setOnClickListener { handleIopaClick() }

        val handleOpgClick = {
            if (cbOpg.isChecked) openUploadDialog("opg")
        }
        containerUploadOpg.setOnClickListener { handleOpgClick() }
        tvUploadOpgTitle.setOnClickListener { handleOpgClick() }
        tvUploadOpgSub.setOnClickListener { handleOpgClick() }

        val handleCbctClick = {
            if (cbCbct.isChecked) openUploadDialog("cbct")
        }
        containerUploadCbct.setOnClickListener { handleCbctClick() }
        tvUploadCbctTitle.setOnClickListener { handleCbctClick() }
        tvUploadCbctSub.setOnClickListener { handleCbctClick() }

        // Hide overlay helper
        fun dismissPopups() {
            dimOverlay.visibility = View.GONE
            layoutUploadDialog.visibility = View.GONE
            layoutPhotoPicker.visibility = View.GONE
        }

        fun updateUploadState(fileName: String, fileSize: String) {
            when (activeUploadTarget) {
                "iopa" -> {
                    iopaUploaded = true
                    tvUploadIopaTitle.text = fileName
                    tvUploadIopaTitle.setTextColor(getColor(R.color.bg_dark_blue))
                    tvUploadIopaSub.text = "Size: $fileSize • Successfully Uploaded"
                    tvUploadIopaSub.setTextColor(getColor(R.color.status_green))
                }
                "opg" -> {
                    opgUploaded = true
                    tvUploadOpgTitle.text = fileName
                    tvUploadOpgTitle.setTextColor(getColor(R.color.bg_dark_blue))
                    tvUploadOpgSub.text = "Size: $fileSize • Successfully Uploaded"
                    tvUploadOpgSub.setTextColor(getColor(R.color.status_green))
                }
                "cbct" -> {
                    cbctUploaded = true
                    tvUploadCbctTitle.text = fileName
                    tvUploadCbctTitle.setTextColor(getColor(R.color.bg_dark_blue))
                    tvUploadCbctSub.text = "Size: $fileSize • Successfully Uploaded"
                    tvUploadCbctSub.setTextColor(getColor(R.color.status_green))
                }
            }
            dismissPopups()
            Toast.makeText(this, "$fileName uploaded successfully", Toast.LENGTH_SHORT).show()
        }

        btnDialogCamera.setOnClickListener {
            layoutUploadDialog.visibility = View.GONE
            launchCamera(::updateUploadState)
        }

        btnDialogDevice.setOnClickListener {
            layoutUploadDialog.visibility = View.GONE
            launchPicker(::updateUploadState)
        }

        btnDismissWarning.setOnClickListener {
            dismissPopups()
        }

        dimOverlay.setOnClickListener {
            dismissPopups()
        }

        // Picker selection listeners
        thumb1.setOnClickListener { launchPicker(::updateUploadState) }
        thumb2.setOnClickListener { launchPicker(::updateUploadState) }
        thumb3.setOnClickListener { launchPicker(::updateUploadState) }
        thumb4.setOnClickListener { launchPicker(::updateUploadState) }
        thumb5.setOnClickListener { launchPicker(::updateUploadState) }
        thumb6.setOnClickListener { launchPicker(::updateUploadState) }

        // Action Buttons
        val btnWizardBack = findViewById<LinearLayout>(R.id.btnWizardBack)
        val btnWizardNext = findViewById<LinearLayout>(R.id.btnWizardNext)

        btnWizardBack.setOnClickListener {
            // Finish this step, return to Step 2
            finish()
        }

        btnWizardNext.setOnClickListener {
            if (cbIopa.isChecked && !iopaUploaded) {
                Toast.makeText(this, "Please upload the IOPA scan to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cbOpg.isChecked && !opgUploaded) {
                Toast.makeText(this, "Please upload the OPG scan to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cbCbct.isChecked && !cbctUploaded) {
                Toast.makeText(this, "Please upload the CBCT scan to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, OMFSWizardStep4Activity::class.java).apply {
                currentPatientId?.let { putExtra("patient_id", it) }
                putExtra("patient_name", name)
                putExtra("patient_age", age)
                putExtra("patient_gender", gender)
                putExtra("patient_procedure", procedure)
                putExtra("patient_asa", asa)
                putStringArrayListExtra("patient_allergies", allergies)
                currentPatientId?.let { putExtra("patient_id", it) }
                putExtra("vital_bp_sys", this@OMFSWizardStep3Activity.intent.getStringExtra("vital_bp_sys"))
                putExtra("vital_bp_dia", this@OMFSWizardStep3Activity.intent.getStringExtra("vital_bp_dia"))
                putExtra("vital_pulse", this@OMFSWizardStep3Activity.intent.getStringExtra("vital_pulse"))
                putExtra("vital_temp", this@OMFSWizardStep3Activity.intent.getStringExtra("vital_temp"))
                putExtra("vital_resp", this@OMFSWizardStep3Activity.intent.getStringExtra("vital_resp"))
                putExtra("vital_spo2", this@OMFSWizardStep3Activity.intent.getStringExtra("vital_spo2"))
            }
            startActivity(intent)
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

    private fun launchPicker(uiUpdater: (String, String) -> Unit) {
        if (!authRepository.isLoggedIn()) {
            Toast.makeText(this, "Please sign in before uploading files", Toast.LENGTH_LONG).show()
            return
        }
        if (currentPatientId == null) {
            currentPatientId = 1L
        }
        pendingUiUpdate = uiUpdater
        filePicker.launch("*/*")
    }

    private fun launchCamera(uiUpdater: (String, String) -> Unit) {
        if (!authRepository.isLoggedIn()) {
            Toast.makeText(this, "Please sign in before capturing files", Toast.LENGTH_LONG).show()
            return
        }
        if (currentPatientId == null) {
            currentPatientId = 1L
        }
        pendingUiUpdate = uiUpdater
        cameraPicker.launch(null)
    }

    private fun uploadSelectedFile(uri: Uri) {
        val uiUpdater = pendingUiUpdate ?: return
        val patientId = currentPatientId ?: 1L
        currentPatientId = patientId
        lifecycleScope.launch {
            fileRepository.uploadFile(contentResolver, uri, "radiology")
                .onSuccess { fileUrl ->
                    val request = buildRadiologyRequest(fileUrl)
                    radiologyRepository.saveRadiology(patientId, request)
                        .onSuccess {
                            uiUpdater(fileUrl.substringAfterLast('/'), formatFileSize(uri))
                        }
                        .onFailure {
                            Toast.makeText(this@OMFSWizardStep3Activity, it.message ?: "Unable to save radiology data", Toast.LENGTH_LONG).show()
                        }
                }
                .onFailure {
                    Toast.makeText(this@OMFSWizardStep3Activity, it.message ?: "Upload failed", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun buildRadiologyRequest(fileUrl: String): RadiologyRequest {
        return RadiologyRequest(
            iopaTaken = iopaUploaded || activeUploadTarget == "iopa",
            iopaFileUrl = if (activeUploadTarget == "iopa") fileUrl else null,
            opgTaken = opgUploaded || activeUploadTarget == "opg",
            opgFileUrl = if (activeUploadTarget == "opg") fileUrl else null,
            cbctTaken = cbctUploaded || activeUploadTarget == "cbct",
            cbctFileUrl = if (activeUploadTarget == "cbct") fileUrl else null
        )
    }

    private fun formatFileSize(uri: Uri): String {
        val descriptor = contentResolver.openAssetFileDescriptor(uri, "r")
        val sizeBytes = descriptor?.length ?: -1L
        descriptor?.close()
        if (sizeBytes <= 0L) return "Uploaded"
        val sizeKb = sizeBytes / 1024.0
        return if (sizeKb >= 1024) {
            String.format("%.1f MB", sizeKb / 1024.0)
        } else {
            String.format("%.0f KB", sizeKb)
        }
    }
}
