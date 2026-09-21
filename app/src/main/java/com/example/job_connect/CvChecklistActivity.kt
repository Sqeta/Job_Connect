package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CvChecklistActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CvChecklistActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var checkContactDetails: CheckBox
    private lateinit var checkEducation: CheckBox
    private lateinit var checkSkillsProjects: CheckBox
    private lateinit var checkCvDocument: CheckBox
    private lateinit var tvChecklistProgress: TextView
    private lateinit var btnUpdateChecklist: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cv_checklist)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupCheckListeners()
        setupUpdateButton()
        setupBottomNavigation()
        loadChecklistProgress()
    }

    private fun initialiseViews() {
        checkContactDetails =
            findViewById(R.id.checkContactDetails)

        checkEducation =
            findViewById(R.id.checkEducation)

        checkSkillsProjects =
            findViewById(R.id.checkSkillsProjects)

        checkCvDocument =
            findViewById(R.id.checkCvDocument)

        tvChecklistProgress =
            findViewById(R.id.tvChecklistProgress)

        btnUpdateChecklist =
            findViewById(R.id.btnUpdateChecklist)
    }

    private fun setupCheckListeners() {
        checkContactDetails.setOnCheckedChangeListener { _, _ ->
            updateProgressDisplay()
        }

        checkEducation.setOnCheckedChangeListener { _, _ ->
            updateProgressDisplay()
        }

        checkSkillsProjects.setOnCheckedChangeListener { _, _ ->
            updateProgressDisplay()
        }

        checkCvDocument.setOnCheckedChangeListener { _, _ ->
            updateProgressDisplay()
        }
    }

    private fun setupUpdateButton() {
        btnUpdateChecklist.setOnClickListener {
            saveChecklistProgress()
        }
    }

    private fun calculateProgress(): Int {
        var completedItems = 0

        if (checkContactDetails.isChecked) {
            completedItems++
        }

        if (checkEducation.isChecked) {
            completedItems++
        }

        if (checkSkillsProjects.isChecked) {
            completedItems++
        }

        if (checkCvDocument.isChecked) {
            completedItems++
        }

        return completedItems * 25
    }

    private fun updateProgressDisplay() {
        val progress = calculateProgress()

        tvChecklistProgress.text =
            "CV preparation: $progress% complete"
    }

    private fun saveChecklistProgress() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                this,
                "Please sign in to save your progress.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val progress = calculateProgress()

        val checklistData = hashMapOf(
            "contactDetailsComplete" to
                    checkContactDetails.isChecked,

            "educationComplete" to
                    checkEducation.isChecked,

            "skillsProjectsComplete" to
                    checkSkillsProjects.isChecked,

            "cvDocumentComplete" to
                    checkCvDocument.isChecked,

            "progressPercentage" to progress
        )

        btnUpdateChecklist.isEnabled = false
        btnUpdateChecklist.text = "Saving..."

        firestore
            .collection("users")
            .document(userId)
            .collection("progress")
            .document("cvChecklist")
            .set(checklistData)
            .addOnSuccessListener {
                btnUpdateChecklist.isEnabled = true
                btnUpdateChecklist.text = "Update progress"

                Toast.makeText(
                    this,
                    "CV progress saved: $progress%",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "CV checklist saved: $progress%"
                )
                startActivity(
                    Intent(
                        this,
                        AchievementsActivity::class.java
                    )
                )
            }
            .addOnFailureListener { exception ->
                btnUpdateChecklist.isEnabled = true
                btnUpdateChecklist.text = "Update progress"

                Toast.makeText(
                    this,
                    "Could not save your progress.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to save CV checklist",
                    exception
                )
            }
    }

    private fun loadChecklistProgress() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .collection("progress")
            .document("cvChecklist")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    checkContactDetails.isChecked =
                        document.getBoolean(
                            "contactDetailsComplete"
                        ) ?: false

                    checkEducation.isChecked =
                        document.getBoolean(
                            "educationComplete"
                        ) ?: false

                    checkSkillsProjects.isChecked =
                        document.getBoolean(
                            "skillsProjectsComplete"
                        ) ?: false

                    checkCvDocument.isChecked =
                        document.getBoolean(
                            "cvDocumentComplete"
                        ) ?: false

                    updateProgressDisplay()

                    Log.d(
                        TAG,
                        "CV checklist loaded"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load CV checklist",
                    exception
                )
            }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.checklistNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.checklistNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        findViewById<TextView>(
            R.id.checklistNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.checklistNavProfile
        ).setOnClickListener {
            Toast.makeText(
                this,
                "Profile page is coming soon.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}