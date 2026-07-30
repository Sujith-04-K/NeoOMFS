package com.simats.neoomfs.activities

import com.simats.neoomfs.R

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.neoomfs.network.RetrofitClient
import com.simats.neoomfs.session.AuthSessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        RetrofitClient.initialize(applicationContext)
        val sessionManager = AuthSessionManager(applicationContext)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgressPercent = findViewById<TextView>(R.id.tvProgressPercent)

        // Animate the progress bar from 0% to 100% over 2 seconds
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 2000
        animator.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Int
            progressBar.progress = progress
            tvProgressPercent.text = "$progress%"
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val nextActivity = if (sessionManager.isLoggedIn()) {
                    MainActivity::class.java
                } else {
                    OnboardingActivity1::class.java
                }
                val intent = Intent(this@SplashActivity, nextActivity)
                startActivity(intent)
                finish()
            }
        })

        animator.start()
    }
}
