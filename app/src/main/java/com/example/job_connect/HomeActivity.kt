package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser == null) {
            Log.w(TAG, "No authenticated user found")

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        displayUserGreeting()
    }

    private fun displayUserGreeting() {
        val greetingText = findViewById<TextView>(R.id.tvGreeting)
        val userName = firebaseAuth.currentUser?.displayName
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
            ?: "Job Seeker"

        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }

        greetingText.text = "$greeting, $userName"

        Log.d(TAG, "Personalised greeting displayed")
    }
}