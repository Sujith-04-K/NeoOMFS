package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        val tvSkip = findViewById<TextView>(R.id.tvSkip)
        val btnNext = findViewById<TextView>(R.id.btnNext)

        tvSkip.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, OnboardingActivity2::class.java)
            startActivity(intent)
        }
    }
}
