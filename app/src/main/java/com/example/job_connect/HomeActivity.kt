package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
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
            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
            return
        }

        setContentView(R.layout.activity_home)

        displayUserGreeting()
        setupHomeSearch()
        setupBottomNavigation()
        setupRecommendedJobCards()
    }

    private fun displayUserGreeting() {
        val greetingText =
            findViewById<TextView>(R.id.tvGreeting)

        val userName = firebaseAuth.currentUser
            ?.displayName
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
            ?: "Job Seeker"

        val hour = Calendar.getInstance()
            .get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }

        greetingText.text =
            "$greeting, $userName"
    }

    private fun setupHomeSearch() {
        val homeSearch =
            findViewById<EditText>(R.id.etHomeSearch)

        homeSearch.isFocusable = false
        homeSearch.isCursorVisible = false
        homeSearch.isClickable = true

        homeSearch.setOnClickListener {
            openSearchScreen()
        }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.navHome
        ).setOnClickListener {
            // Already on Home.
        }

        findViewById<TextView>(
            R.id.navSearch
        ).setOnClickListener {
            openSearchScreen()
        }

        findViewById<TextView>(
            R.id.navSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.navProfile
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )

            Log.d(TAG, "Profile navigation selected")
        }
    }

    private fun setupRecommendedJobCards() {
        val androidDeveloperCard =
            findViewById<LinearLayout>(
                R.id.cardAndroidDeveloper
            )

        val supportInternCard =
            findViewById<LinearLayout>(
                R.id.cardSupportIntern
            )

        androidDeveloperCard.setOnClickListener {
            openSearchScreen("Android Developer")
        }

        supportInternCard.setOnClickListener {
            openSearchScreen("IT Support Intern")
        }
    }

    private fun openSearchScreen(
        keyword: String = ""
    ) {
        val searchIntent =
            Intent(this, SearchActivity::class.java)

        searchIntent.putExtra(
            "SEARCH_KEYWORD",
            keyword
        )

        startActivity(searchIntent)
    }
}