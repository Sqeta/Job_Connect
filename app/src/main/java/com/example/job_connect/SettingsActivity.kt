package com.example.job_connect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.job_connect.worker.JobAlertWorker
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SettingsActivity"

        private const val PERIODIC_JOB_ALERT_WORK =
            "periodic_job_alert_work"

        private const val TEST_JOB_ALERT_WORK =
            "test_job_alert_work"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var spinnerLanguage: Spinner
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var switchJobAlerts: SwitchMaterial
    private lateinit var btnSaveSettings: Button

    private val languages = listOf(
        "English",
        "Afrikaans",
        "isiZulu",
        "Sesotho"
    )

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->

            if (permissionGranted) {
                scheduleJobAlerts()

                Toast.makeText(
                    this,
                    "Job alerts have been enabled.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                switchJobAlerts.isChecked = false
                cancelJobAlerts()

                Toast.makeText(
                    this,
                    "Notification permission is required for job alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupLanguageSpinner()
        setupButtons()
        setupBottomNavigation()
        loadSettings()
    }

    private fun initialiseViews() {
        spinnerLanguage =
            findViewById(R.id.spinnerLanguage)

        switchDarkMode =
            findViewById(R.id.switchDarkMode)

        switchJobAlerts =
            findViewById(R.id.switchJobAlerts)

        btnSaveSettings =
            findViewById(R.id.btnSaveSettings)

        // Dark mode is temporarily disabled.
        switchDarkMode.isChecked = false
        switchDarkMode.isEnabled = false
    }

    private fun setupLanguageSpinner() {
        val languageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )

        languageAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerLanguage.adapter = languageAdapter
    }

    private fun setupButtons() {
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        findViewById<Button>(
            R.id.btnSignOut
        ).setOnClickListener {
            signOutUser()
        }
    }

    private fun saveSettings() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            openLoginScreen()
            return
        }

        val selectedLanguage =
            spinnerLanguage.selectedItem.toString()

        val jobAlertsEnabled =
            switchJobAlerts.isChecked

        val settings = hashMapOf(
            "language" to selectedLanguage,
            "darkModeEnabled" to false,
            "notificationsEnabled" to jobAlertsEnabled
        )

        btnSaveSettings.isEnabled = false
        btnSaveSettings.text = "Saving..."

        firestore
            .collection("users")
            .document(userId)
            .set(
                settings,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                btnSaveSettings.isEnabled = true
                btnSaveSettings.text = "Save settings"

                if (jobAlertsEnabled) {
                    enableJobAlerts()
                } else {
                    cancelJobAlerts()

                    Toast.makeText(
                        this,
                        "Settings saved. Job alerts are disabled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Log.d(
                    TAG,
                    "User settings saved successfully"
                )
            }
            .addOnFailureListener { exception ->
                btnSaveSettings.isEnabled = true
                btnSaveSettings.text = "Save settings"

                Toast.makeText(
                    this,
                    "Could not save settings.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to save settings",
                    exception
                )
            }
    }

    private fun enableJobAlerts() {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            scheduleJobAlerts()

            Toast.makeText(
                this,
                "Settings saved. Job alerts are enabled.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun scheduleJobAlerts() {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<JobAlertWorker>(
                24,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                PERIODIC_JOB_ALERT_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )

        // Runs once immediately so the feature can be tested.
        val testWorkRequest =
            OneTimeWorkRequestBuilder<JobAlertWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                TEST_JOB_ALERT_WORK,
                androidx.work.ExistingWorkPolicy.REPLACE,
                testWorkRequest
            )

        Log.d(
            TAG,
            "Periodic and test job alerts scheduled"
        )
    }

    private fun cancelJobAlerts() {
        val workManager =
            WorkManager.getInstance(
                applicationContext
            )

        workManager.cancelUniqueWork(
            PERIODIC_JOB_ALERT_WORK
        )

        workManager.cancelUniqueWork(
            TEST_JOB_ALERT_WORK
        )

        Log.d(
            TAG,
            "Job alerts cancelled"
        )
    }

    private fun loadSettings() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            openLoginScreen()
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val savedLanguage =
                    document.getString("language")
                        ?: "English"

                val languagePosition =
                    languages.indexOf(savedLanguage)

                if (languagePosition >= 0) {
                    spinnerLanguage.setSelection(
                        languagePosition
                    )
                }

                switchDarkMode.isChecked = false
                switchDarkMode.isEnabled = false

                switchJobAlerts.isChecked =
                    document.getBoolean(
                        "notificationsEnabled"
                    ) ?: true

                Log.d(
                    TAG,
                    "Settings loaded successfully"
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to load settings",
                    exception
                )
            }
    }

    private fun signOutUser() {
        cancelJobAlerts()
        firebaseAuth.signOut()

        Toast.makeText(
            this,
            "Signed out successfully",
            Toast.LENGTH_SHORT
        ).show()

        val mainIntent =
            Intent(this, MainActivity::class.java)

        mainIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(mainIntent)
        finish()
    }

    private fun openLoginScreen() {
        val loginIntent =
            Intent(this, LoginActivity::class.java)

        loginIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(loginIntent)
        finish()
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.settingsNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.settingsNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        findViewById<TextView>(
            R.id.settingsNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )
        }

        findViewById<TextView>(
            R.id.settingsNavProfile
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )

            finish()
        }
    }
}