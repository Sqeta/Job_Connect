package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var progressProfile: View
    private lateinit var profileScrollView: View
    private lateinit var tvProfileIcon: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfileSkills: TextView
    private lateinit var tvProfilePreferences: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupButtons()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun initialiseViews() {
        progressProfile =
            findViewById(R.id.progressProfile)

        profileScrollView =
            findViewById(R.id.profileScrollView)

        tvProfileIcon =
            findViewById(R.id.tvProfileIcon)

        tvProfileName =
            findViewById(R.id.tvProfileName)

        tvProfileEmail =
            findViewById(R.id.tvProfileEmail)

        tvProfileSkills =
            findViewById(R.id.tvProfileSkills)

        tvProfilePreferences =
            findViewById(R.id.tvProfilePreferences)
    }

    private fun setupButtons() {
        findViewById<Button>(
            R.id.btnEditProfile
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    EditProfileActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.btnProfileAchievements
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AchievementsActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.btnProfileSettings
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }
    }

    private fun loadProfile() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
            return
        }

        showLoading(true)

        tvProfileEmail.text =
            currentUser.email ?: "Email not provided"

        firestore
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                val fullName =
                    document.getString("fullName")
                        ?: currentUser.displayName
                        ?: "Job Seeker"

                val education =
                    document.getString("education")
                        .orEmpty()

                val skills =
                    convertListToText(
                        document.get("skills")
                    )

                val preferredLocations =
                    convertListToText(
                        document.get("preferredLocations")
                    )

                val preferredJobTypes =
                    convertListToText(
                        document.get("preferredJobTypes")
                    )

                tvProfileName.text = fullName

                tvProfileIcon.text =
                    fullName.firstOrNull()
                        ?.uppercase()
                        ?: "J"

                tvProfileSkills.text =
                    if (skills.isBlank()) {
                        if (education.isBlank()) {
                            "No skills added yet"
                        } else {
                            "Education: $education"
                        }
                    } else {
                        if (education.isBlank()) {
                            skills
                        } else {
                            "$skills\nEducation: $education"
                        }
                    }

                val preferences = buildList {
                    if (preferredJobTypes.isNotBlank()) {
                        add(preferredJobTypes)
                    }

                    if (preferredLocations.isNotBlank()) {
                        add(preferredLocations)
                    }
                }.joinToString(" | ")

                tvProfilePreferences.text =
                    preferences.ifBlank {
                        "No preferences added yet"
                    }

                Log.d(
                    TAG,
                    "Profile loaded successfully"
                )
            }
            .addOnFailureListener { exception ->
                showLoading(false)

                Toast.makeText(
                    this,
                    "Could not load your profile.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to load profile",
                    exception
                )
            }
    }

    private fun convertListToText(value: Any?): String {
        return when (value) {
            is List<*> -> value
                .filterIsInstance<String>()
                .filter { it.isNotBlank() }
                .joinToString(", ")

            is String -> value

            else -> ""
        }
    }



    private fun showLoading(isLoading: Boolean) {
        progressProfile.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        profileScrollView.visibility =
            if (isLoading) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.profileNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.profileNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        findViewById<TextView>(
            R.id.profileNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.profileNavProfile
        ).setOnClickListener {
            // Already on the Profile page.
        }
    }
}