package com.example.job_connect

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AchievementsActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvAchievementSummary: TextView

    private lateinit var tvProfileBadgeIcon: TextView
    private lateinit var tvProfileBadgeStatus: TextView

    private lateinit var tvApplicationBadgeIcon: TextView
    private lateinit var tvApplicationBadgeStatus: TextView

    private lateinit var tvSearcherBadgeIcon: TextView
    private lateinit var tvSearcherBadgeStatus: TextView

    private var profileBadgeUnlocked = false
    private var applicationBadgeUnlocked = false
    private var searcherBadgeUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupButtons()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadAchievements()
    }

    private fun initialiseViews() {
        tvAchievementSummary =
            findViewById(R.id.tvAchievementSummary)

        tvProfileBadgeIcon =
            findViewById(R.id.tvProfileBadgeIcon)

        tvProfileBadgeStatus =
            findViewById(R.id.tvProfileBadgeStatus)

        tvApplicationBadgeIcon =
            findViewById(R.id.tvApplicationBadgeIcon)

        tvApplicationBadgeStatus =
            findViewById(R.id.tvApplicationBadgeStatus)

        tvSearcherBadgeIcon =
            findViewById(R.id.tvSearcherBadgeIcon)

        tvSearcherBadgeStatus =
            findViewById(R.id.tvSearcherBadgeStatus)
    }

    private fun setupButtons() {
        findViewById<Button>(
            R.id.btnOpenProfile
        ).setOnClickListener {
            openProfile()
        }
    }

    private fun openProfile() {
        startActivity(
            Intent(
                this,
                ProfileActivity::class.java
            )
        )
    }

    private fun loadAchievements() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
            return
        }

        profileBadgeUnlocked = false
        applicationBadgeUnlocked = false
        searcherBadgeUnlocked = false

        resetAchievementViews()
        loadProfileAchievement(userId)
        loadApplicationAchievement(userId)
        loadSearcherAchievement(userId)
    }

    private fun loadProfileAchievement(
        userId: String
    ) {
        firestore
            .collection("users")
            .document(userId)
            .collection("progress")
            .document("cvChecklist")
            .get()
            .addOnSuccessListener { document ->
                val progress =
                    document.getLong(
                        "progressPercentage"
                    )?.toInt() ?: 0

                profileBadgeUnlocked =
                    progress >= 75

                if (profileBadgeUnlocked) {
                    unlockBadge(
                        tvProfileBadgeIcon,
                        tvProfileBadgeStatus,
                        "Unlocked — CV is $progress% complete"
                    )
                } else {
                    tvProfileBadgeStatus.text =
                        "Complete at least 75% of your CV checklist"
                }

                updateAchievementSummary()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load profile achievement",
                    exception
                )
            }
    }

    private fun loadApplicationAchievement(
        userId: String
    ) {
        firestore
            .collection("users")
            .document(userId)
            .collection("applications")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                applicationBadgeUnlocked =
                    !snapshot.isEmpty

                if (applicationBadgeUnlocked) {
                    unlockBadge(
                        tvApplicationBadgeIcon,
                        tvApplicationBadgeStatus,
                        "Unlocked — first application recorded"
                    )
                } else {
                    tvApplicationBadgeStatus.text =
                        "Record your first application"
                }

                updateAchievementSummary()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load application achievement",
                    exception
                )
            }
    }

    private fun loadSearcherAchievement(
        userId: String
    ) {
        firestore
            .collection("users")
            .document(userId)
            .collection("savedJobs")
            .get()
            .addOnSuccessListener { snapshot ->
                searcherBadgeUnlocked =
                    snapshot.size() >= 3

                if (searcherBadgeUnlocked) {
                    unlockBadge(
                        tvSearcherBadgeIcon,
                        tvSearcherBadgeStatus,
                        "Unlocked — ${snapshot.size()} jobs saved"
                    )
                } else {
                    tvSearcherBadgeStatus.text =
                        "Save 3 jobs — ${snapshot.size()} saved"
                }

                updateAchievementSummary()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load searcher achievement",
                    exception
                )
            }
    }

    private fun unlockBadge(
        badgeIcon: TextView,
        badgeStatus: TextView,
        message: String
    ) {
        badgeIcon.text = "✓"
        badgeIcon.setTextColor(Color.WHITE)

        badgeIcon.setBackgroundColor(
            Color.parseColor("#13856E")
        )

        badgeStatus.text = message

        badgeStatus.setTextColor(
            Color.parseColor("#13856E")
        )
    }

    private fun resetAchievementViews() {
        val icons = listOf(
            tvProfileBadgeIcon,
            tvApplicationBadgeIcon,
            tvSearcherBadgeIcon
        )

        icons.forEach { icon ->
            icon.text = "★"

            icon.setTextColor(
                Color.parseColor("#1769C2")
            )

            icon.setBackgroundColor(
                Color.parseColor("#DCEBFF")
            )
        }

        tvProfileBadgeStatus.text =
            "Complete your CV checklist"

        tvApplicationBadgeStatus.text =
            "Record your first application"

        tvSearcherBadgeStatus.text =
            "Save three job opportunities"

        updateAchievementSummary()
    }

    private fun updateAchievementSummary() {
        var unlockedCount = 0

        if (profileBadgeUnlocked) {
            unlockedCount++
        }

        if (applicationBadgeUnlocked) {
            unlockedCount++
        }

        if (searcherBadgeUnlocked) {
            unlockedCount++
        }

        tvAchievementSummary.text =
            "$unlockedCount of 3 achievements unlocked"
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.achievementsNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.achievementsNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        findViewById<TextView>(
            R.id.achievementsNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.achievementsNavProfile
        ).setOnClickListener {
            openProfile()
        }
    }
}