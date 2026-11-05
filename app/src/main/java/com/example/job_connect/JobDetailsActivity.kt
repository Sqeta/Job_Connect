package com.example.job_connect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.job_connect.data.local.CachedJobEntity
import com.example.job_connect.data.local.JobConnectDatabase
import com.example.job_connect.worker.OfflineJobSyncWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JobDetailsActivity :
    AppCompatActivity() {

    companion object {
        private const val TAG =
            "JobDetailsActivity"

        private const val OFFLINE_SYNC_WORK =
            "offline_saved_job_sync"

        const val EXTRA_JOB_ID =
            "EXTRA_JOB_ID"

        const val EXTRA_TITLE =
            "EXTRA_TITLE"

        const val EXTRA_COMPANY =
            "EXTRA_COMPANY"

        const val EXTRA_LOCATION =
            "EXTRA_LOCATION"

        const val EXTRA_CONTRACT_TYPE =
            "EXTRA_CONTRACT_TYPE"

        const val EXTRA_DESCRIPTION =
            "EXTRA_DESCRIPTION"

        const val EXTRA_REDIRECT_URL =
            "EXTRA_REDIRECT_URL"
    }

    private lateinit var firebaseAuth:
            FirebaseAuth

    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var database:
            JobConnectDatabase

    private var jobId = ""
    private var title = ""
    private var company = ""
    private var location = ""
    private var contractType = ""
    private var description = ""
    private var redirectUrl = ""

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_job_details
        )

        firebaseAuth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()

        database =
            JobConnectDatabase.getDatabase(
                applicationContext
            )

        readJobInformation()
        displayJobInformation()
        setupButtons()
        setupBottomNavigation()
    }

    private fun readJobInformation() {
        jobId =
            intent
                .getStringExtra(EXTRA_JOB_ID)
                .orEmpty()

        title =
            intent
                .getStringExtra(EXTRA_TITLE)
                ?: "Job opportunity"

        company =
            intent
                .getStringExtra(EXTRA_COMPANY)
                ?: "Company not provided"

        location =
            intent
                .getStringExtra(EXTRA_LOCATION)
                ?: "Location not provided"

        contractType =
            intent
                .getStringExtra(
                    EXTRA_CONTRACT_TYPE
                )
                ?: "Job opportunity"

        description =
            intent
                .getStringExtra(
                    EXTRA_DESCRIPTION
                )
                ?: "No job description was provided."

        redirectUrl =
            intent
                .getStringExtra(
                    EXTRA_REDIRECT_URL
                )
                .orEmpty()

        if (jobId.isBlank()) {
            jobId =
                redirectUrl
                    .hashCode()
                    .toString()
                    .replace("-", "n")
        }
    }

    private fun displayJobInformation() {
        val headerTitle =
            findViewById<TextView>(
                R.id.tvDetailHeaderTitle
            )

        val jobIcon =
            findViewById<TextView>(
                R.id.tvDetailIcon
            )

        val jobTitle =
            findViewById<TextView>(
                R.id.tvDetailTitle
            )

        val jobCompany =
            findViewById<TextView>(
                R.id.tvDetailCompany
            )

        val jobLocationType =
            findViewById<TextView>(
                R.id.tvDetailLocationType
            )

        val jobDescription =
            findViewById<TextView>(
                R.id.tvDetailDescription
            )

        headerTitle.text = title
        jobTitle.text = title
        jobCompany.text = company

        jobLocationType.text =
            "$location | ${
                formatContractType(
                    contractType
                )
            }"

        jobIcon.text =
            company
                .firstOrNull()
                ?.uppercase()
                ?: "J"

        jobDescription.text =
            Html.fromHtml(
                description,
                Html.FROM_HTML_MODE_LEGACY
            )
    }

    private fun setupButtons() {
        val saveButton =
            findViewById<Button>(
                R.id.btnDetailSave
            )

        val applyButton =
            findViewById<Button>(
                R.id.btnApplySource
            )

        saveButton.setOnClickListener {
            saveJobOfflineFirst()
        }

        applyButton.setOnClickListener {
            recordApplicationAndOpenSource()
        }
    }

    private fun saveJobOfflineFirst() {
        val userId =
            firebaseAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                this,
                "Please sign in before saving a job.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val cachedJob =
            CachedJobEntity(
                userId = userId,
                jobId = jobId,
                title = title,
                company = company,
                location = location,
                description = description,
                redirectUrl = redirectUrl,
                contractType =
                    contractType.ifBlank {
                        null
                    },
                cachedAt =
                    System.currentTimeMillis(),
                pendingSync = true
            )

        lifecycleScope.launch(
            Dispatchers.IO
        ) {
            try {
                database
                    .cachedJobDao()
                    .insertJob(cachedJob)

                enqueueOfflineSync()

                withContext(
                    Dispatchers.Main
                ) {
                    Toast.makeText(
                        this@JobDetailsActivity,
                        "Job saved. It will sync automatically when connected.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                Log.d(
                    TAG,
                    "Job saved locally: $jobId"
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Failed to save job locally",
                    exception
                )

                withContext(
                    Dispatchers.Main
                ) {
                    Toast.makeText(
                        this@JobDetailsActivity,
                        "Could not save this job.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun enqueueOfflineSync() {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<
                    OfflineJobSyncWorker
                    >()
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(
                applicationContext
            )
            .enqueueUniqueWork(
                OFFLINE_SYNC_WORK,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )

        Log.d(
            TAG,
            "Offline synchronisation scheduled"
        )
    }

    private fun recordApplicationAndOpenSource() {
        val userId =
            firebaseAuth.currentUser?.uid

        if (redirectUrl.isBlank()) {
            Toast.makeText(
                this,
                "Application link is unavailable.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (userId == null) {
            openSourceWebsite()
            return
        }

        val application =
            hashMapOf(
                "jobId" to jobId,
                "title" to title,
                "company" to company,
                "location" to location,
                "redirectUrl" to redirectUrl,
                "status" to "Applied",
                "appliedAt" to
                        FieldValue.serverTimestamp()
            )

        firestore
            .collection("users")
            .document(userId)
            .collection("applications")
            .document(jobId)
            .set(application)
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "Application recorded: $jobId"
                )

                openSourceWebsite()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to record application",
                    exception
                )

                openSourceWebsite()
            }
    }

    private fun openSourceWebsite() {
        try {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(redirectUrl)
                )

            startActivity(browserIntent)
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                "Could not open the application website.",
                Toast.LENGTH_LONG
            ).show()

            Log.e(
                TAG,
                "Failed to open application website",
                exception
            )
        }
    }

    private fun formatContractType(
        type: String
    ): String {
        return type
            .replace("_", " ")
            .replaceFirstChar { character ->
                character.uppercase()
            }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.detailsNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(
                    this,
                    HomeActivity::class.java
                )

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.detailsNavSearch
        ).setOnClickListener {
            finish()
        }

        findViewById<TextView>(
            R.id.detailsNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.detailsNavProfile
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }
    }
}