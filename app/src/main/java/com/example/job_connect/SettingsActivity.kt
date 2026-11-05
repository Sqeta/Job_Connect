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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
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
    private lateinit var switchJobAlerts: SwitchMaterial
    private lateinit var btnSaveSettings: Button

    private val languages = listOf(
        "English",
        "isiZulu",
        "Sesotho"
    )

    private val languageCodes = listOf(
        "en",
        "zu",
        "st"
    )

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->

            if (permissionGranted) {
                scheduleJobAlerts()

                Toast.makeText(
                    this,
                    getString(R.string.job_alerts_enabled),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                switchJobAlerts.isChecked = false
                cancelJobAlerts()

                Toast.makeText(
                    this,
                    getString(
                        R.string.notification_permission_required
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

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

        switchJobAlerts =
            findViewById(R.id.switchJobAlerts)

        btnSaveSettings =
            findViewById(R.id.btnSaveSettings)
    }

    private fun setupLanguageSpinner() {
        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                languages
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerLanguage.adapter = adapter

        val currentCode =
            currentLanguageCode()

        val position =
            languageCodes.indexOf(currentCode)

        spinnerLanguage.setSelection(
            if (position >= 0) position else 0
        )
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
        val userId =
            firebaseAuth.currentUser?.uid

        if (userId == null) {
            openLoginScreen()
            return
        }

        val position =
            spinnerLanguage.selectedItemPosition

        val selectedLanguage =
            languages[position]

        val selectedLanguageCode =
            languageCodes[position]

        val jobAlertsEnabled =
            switchJobAlerts.isChecked

        val settings =
            hashMapOf(
                "language" to selectedLanguage,
                "languageCode" to selectedLanguageCode,
                "notificationsEnabled" to jobAlertsEnabled
            )

        btnSaveSettings.isEnabled = false
        btnSaveSettings.text =
            getString(R.string.saving)

        firestore
            .collection("users")
            .document(userId)
            .set(
                settings,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                btnSaveSettings.isEnabled = true
                btnSaveSettings.text =
                    getString(R.string.save_settings)

                if (jobAlertsEnabled) {
                    enableJobAlerts()
                } else {
                    cancelJobAlerts()
                }

                applyLanguage(selectedLanguageCode)

                Toast.makeText(
                    this,
                    getString(R.string.settings_saved),
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "Settings saved successfully"
                )
            }
            .addOnFailureListener { exception ->
                btnSaveSettings.isEnabled = true
                btnSaveSettings.text =
                    getString(R.string.save_settings)

                Toast.makeText(
                    this,
                    getString(
                        R.string.settings_save_failed
                    ),
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to save settings",
                    exception
                )
            }
    }

    private fun loadSettings() {
        val userId =
            firebaseAuth.currentUser?.uid

        if (userId == null) {
            openLoginScreen()
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val languageCode =
                    document.getString(
                        "languageCode"
                    ) ?: currentLanguageCode()

                val languagePosition =
                    languageCodes.indexOf(
                        languageCode
                    )

                spinnerLanguage.setSelection(
                    if (languagePosition >= 0) {
                        languagePosition
                    } else {
                        0
                    }
                )

                val notificationsEnabled =
                    document.getBoolean(
                        "notificationsEnabled"
                    ) ?: true

                switchJobAlerts.isChecked =
                    notificationsEnabled

                Log.d(
                    TAG,
                    "Settings loaded successfully"
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    getString(
                        R.string.settings_load_failed
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                Log.e(
                    TAG,
                    "Failed to load settings",
                    exception
                )
            }
    }

    private fun currentLanguageCode(): String {
        return AppCompatDelegate
            .getApplicationLocales()
            .toLanguageTags()
            .substringBefore("-")
            .ifBlank { "en" }
    }

    private fun applyLanguage(
        languageCode: String
    ) {
        if (currentLanguageCode() == languageCode) {
            return
        }

        val locales =
            LocaleListCompat.forLanguageTags(
                languageCode
            )

        AppCompatDelegate.setApplicationLocales(
            locales
        )
    }

    private fun enableJobAlerts() {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
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
                getString(
                    R.string.job_alerts_enabled
                ),
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

        val periodicRequest =
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
                periodicRequest
            )

        val testRequest =
            OneTimeWorkRequestBuilder<JobAlertWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                TEST_JOB_ALERT_WORK,
                ExistingWorkPolicy.REPLACE,
                testRequest
            )

        Log.d(
            TAG,
            "Job alerts scheduled"
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

    private fun signOutUser() {
        cancelJobAlerts()
        firebaseAuth.signOut()

        Toast.makeText(
            this,
            getString(R.string.signed_out),
            Toast.LENGTH_SHORT
        ).show()

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun openLoginScreen() {
        val intent =
            Intent(
                this,
                LoginActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.settingsNavHome
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    HomeActivity::class.java
                )
            )
            finish()
        }

        findViewById<TextView>(
            R.id.settingsNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SearchActivity::class.java
                )
            )
            finish()
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
            finish()
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