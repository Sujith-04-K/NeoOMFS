package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding3)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val titleText = "Structured <font color='#16A34A'>Treatment<br/>Planning</font>"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvTitle.text = Html.fromHtml(titleText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            tvTitle.text = Html.fromHtml(titleText)
        }

        val tvSkip = findViewById<TextView>(R.id.tvSkip)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnNext = findViewById<TextView>(R.id.btnNext)
        val btnGetStarted = findViewById<TextView>(R.id.btnGetStarted)

        val navigateToMain = {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        tvSkip.setOnClickListener { navigateToMain() }
        btnNext.setOnClickListener { navigateToMain() }
        btnGetStarted.setOnClickListener { navigateToMain() }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
