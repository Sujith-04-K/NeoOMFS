package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        val tvSkip = findViewById<TextView>(R.id.tvSkip)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnNext = findViewById<TextView>(R.id.btnNext)

        tvSkip.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, OnboardingActivity3::class.java)
            startActivity(intent)
        }
    }
}
