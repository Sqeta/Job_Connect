package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.job_connect.adapter.ApplicationAdapter
import com.example.job_connect.data.model.ApplicationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TrackerActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvSavedCount: TextView
    private lateinit var tvAppliedCount: TextView
    private lateinit var tvTrackerStatus: TextView
    private lateinit var progressTracker: ProgressBar
    private lateinit var recyclerApplications: RecyclerView
    private lateinit var applicationAdapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupRecyclerView()
        setupCvChecklistButton()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadTrackerInformation()
    }

    private fun initialiseViews() {
        tvSavedCount = findViewById(R.id.tvSavedCount)
        tvAppliedCount = findViewById(R.id.tvAppliedCount)
        tvTrackerStatus = findViewById(R.id.tvTrackerStatus)
        progressTracker = findViewById(R.id.progressTracker)
        recyclerApplications =
            findViewById(R.id.recyclerApplications)
    }

    private fun setupRecyclerView() {
        applicationAdapter = ApplicationAdapter()

        recyclerApplications.layoutManager =
            LinearLayoutManager(this)

        recyclerApplications.adapter = applicationAdapter
        recyclerApplications.setHasFixedSize(true)
    }

    private fun setupCvChecklistButton() {
        findViewById<Button>(
            R.id.btnOpenCvChecklist
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CvChecklistActivity::class.java
                )
            )

            Log.d(TAG, "CV Checklist opened")
        }
    }

    private fun loadTrackerInformation() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
            return
        }

        showLoading(true)
        loadSavedJobsCount(userId)
        loadApplications(userId)
    }

    private fun loadSavedJobsCount(userId: String) {
        firestore
            .collection("users")
            .document(userId)
            .collection("savedJobs")
            .get()
            .addOnSuccessListener { snapshot ->
                tvSavedCount.text =
                    "Saved: ${snapshot.size()}"

                Log.d(
                    TAG,
                    "Saved jobs count: ${snapshot.size()}"
                )
            }
            .addOnFailureListener { exception ->
                tvSavedCount.text = "Saved: 0"

                Log.e(
                    TAG,
                    "Failed to load saved jobs count",
                    exception
                )
            }
    }

    private fun loadApplications(userId: String) {
        firestore
            .collection("users")
            .document(userId)
            .collection("applications")
            .get()
            .addOnSuccessListener { snapshot ->
                showLoading(false)

                val applications =
                    snapshot.documents.map { document ->
                        ApplicationModel(
                            jobId = document
                                .getString("jobId")
                                ?: document.id,

                            title = document
                                .getString("title")
                                ?: "Job opportunity",

                            company = document
                                .getString("company")
                                ?: "Company not provided",

                            location = document
                                .getString("location")
                                ?: "Location not provided",

                            redirectUrl = document
                                .getString("redirectUrl")
                                ?: "",

                            status = document
                                .getString("status")
                                ?: "Applied"
                        )
                    }

                tvAppliedCount.text =
                    "Applied: ${applications.size}"

                applicationAdapter
                    .updateApplications(applications)

                if (applications.isEmpty()) {
                    recyclerApplications.visibility =
                        View.GONE

                    tvTrackerStatus.visibility =
                        View.VISIBLE

                    tvTrackerStatus.text =
                        "No applications recorded yet."
                } else {
                    recyclerApplications.visibility =
                        View.VISIBLE

                    tvTrackerStatus.visibility =
                        View.GONE
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)

                tvAppliedCount.text = "Applied: 0"
                recyclerApplications.visibility = View.GONE
                tvTrackerStatus.visibility = View.VISIBLE

                tvTrackerStatus.text =
                    "Could not load your applications."

                Toast.makeText(
                    this,
                    "Failed to load tracker information.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to load applications",
                    exception
                )
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressTracker.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            recyclerApplications.visibility = View.GONE
            tvTrackerStatus.visibility = View.GONE
        }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.trackerNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.trackerNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        findViewById<TextView>(
            R.id.trackerNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.trackerNavProfile
        ).setOnClickListener {
            Toast.makeText(
                this,
                "Profile page is coming soon.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}