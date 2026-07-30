package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.utils.startActivityNoAnimation

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnHeaderProfile = findViewById<FrameLayout>(R.id.btnHeaderProfile)
        val btnForceSync = findViewById<LinearLayout>(R.id.btnForceSync)

        // Bottom Navigation views
        val btnNavDashboard = findViewById<FrameLayout>(R.id.btnNavDashboard)
        val btnNavPatients = findViewById<FrameLayout>(R.id.btnNavPatients)
        val btnNavAssess = findViewById<FrameLayout>(R.id.btnNavAssess)
        val btnNavSettings = findViewById<LinearLayout>(R.id.btnNavSettings)

        // Actions Click Triggers
        btnBack.setOnClickListener {
            finish()
        }

        btnHeaderProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        btnForceSync.setOnClickListener {
            Toast.makeText(this, "EHR Database Synced successfully with local node US-EAST-42!", Toast.LENGTH_SHORT).show()
        }

        // Shifting Bottom Navigation Setup
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
            Toast.makeText(this, "You are already in clinical settings", Toast.LENGTH_SHORT).show()
        }
    }
}
