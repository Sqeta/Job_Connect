package com.example.job_connect

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditProfileActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var editProfileScrollView: ScrollView
    private lateinit var progressEditProfile: ProgressBar

    private lateinit var etEditFullName: EditText
    private lateinit var etEditEducation: EditText
    private lateinit var etEditSkills: EditText
    private lateinit var etEditLocations: EditText
    private lateinit var etEditJobTypes: EditText

    private lateinit var btnSaveProfile: Button
    private lateinit var btnCancelEditProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupButtons()
        loadExistingProfile()
    }

    private fun initialiseViews() {
        editProfileScrollView =
            findViewById(R.id.editProfileScrollView)

        progressEditProfile =
            findViewById(R.id.progressEditProfile)

        etEditFullName =
            findViewById(R.id.etEditFullName)

        etEditEducation =
            findViewById(R.id.etEditEducation)

        etEditSkills =
            findViewById(R.id.etEditSkills)

        etEditLocations =
            findViewById(R.id.etEditLocations)

        etEditJobTypes =
            findViewById(R.id.etEditJobTypes)

        btnSaveProfile =
            findViewById(R.id.btnSaveProfile)

        btnCancelEditProfile =
            findViewById(R.id.btnCancelEditProfile)
    }

    private fun setupButtons() {
        btnSaveProfile.setOnClickListener {
            validateAndSaveProfile()
        }

        btnCancelEditProfile.setOnClickListener {
            finish()
        }
    }

    private fun loadExistingProfile() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Please sign in again.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        showLoading(true)

        firestore
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                etEditFullName.setText(
                    document.getString("fullName")
                        ?: currentUser.displayName
                            .orEmpty()
                )

                etEditEducation.setText(
                    document.getString("education")
                        .orEmpty()
                )

                etEditSkills.setText(
                    convertListToText(
                        document.get("skills")
                    )
                )

                etEditLocations.setText(
                    convertListToText(
                        document.get("preferredLocations")
                    )
                )

                etEditJobTypes.setText(
                    convertListToText(
                        document.get("preferredJobTypes")
                    )
                )

                Log.d(
                    TAG,
                    "Existing profile loaded successfully"
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

    private fun validateAndSaveProfile() {
        val fullName =
            etEditFullName.text.toString().trim()

        val education =
            etEditEducation.text.toString().trim()

        val skills =
            convertTextToList(
                etEditSkills.text.toString()
            )

        val preferredLocations =
            convertTextToList(
                etEditLocations.text.toString()
            )

        val preferredJobTypes =
            convertTextToList(
                etEditJobTypes.text.toString()
            )

        if (fullName.isBlank()) {
            etEditFullName.error =
                "Please enter your full name"

            etEditFullName.requestFocus()
            return
        }

        if (fullName.length < 2) {
            etEditFullName.error =
                "Please enter a valid full name"

            etEditFullName.requestFocus()
            return
        }

        saveProfile(
            fullName = fullName,
            education = education,
            skills = skills,
            preferredLocations = preferredLocations,
            preferredJobTypes = preferredJobTypes
        )
    }

    private fun saveProfile(
        fullName: String,
        education: String,
        skills: List<String>,
        preferredLocations: List<String>,
        preferredJobTypes: List<String>
    ) {
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Please sign in again.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        showLoading(true)

        val profileComplete =
            fullName.isNotBlank() &&
                    education.isNotBlank() &&
                    skills.isNotEmpty() &&
                    preferredLocations.isNotEmpty() &&
                    preferredJobTypes.isNotEmpty()

        val profileUpdates = hashMapOf<String, Any>(
            "fullName" to fullName,
            "education" to education,
            "skills" to skills,
            "preferredLocations" to preferredLocations,
            "preferredJobTypes" to preferredJobTypes,
            "profileCompleted" to profileComplete,
            "userId" to currentUser.uid,
            "email" to (currentUser.email ?: "")
        )

        firestore
            .collection("users")
            .document(currentUser.uid)
            .set(
                profileUpdates,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                updateFirebaseDisplayName(fullName)

                Toast.makeText(
                    this,
                    "Profile updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "Profile saved successfully"
                )

                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { exception ->
                showLoading(false)

                Toast.makeText(
                    this,
                    "Could not save your profile. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to save profile",
                    exception
                )
            }
    }

    private fun updateFirebaseDisplayName(
        fullName: String
    ) {
        val profileRequest =
            UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

        firebaseAuth.currentUser
            ?.updateProfile(profileRequest)
            ?.addOnSuccessListener {
                Log.d(
                    TAG,
                    "Firebase display name updated"
                )
            }
            ?.addOnFailureListener { exception ->
                Log.w(
                    TAG,
                    "Firestore saved, but display name was not updated",
                    exception
                )
            }
    }

    private fun convertTextToList(
        text: String
    ): List<String> {
        return text
            .split(",")
            .map { item ->
                item.trim()
            }
            .filter { item ->
                item.isNotBlank()
            }
            .distinct()
    }

    private fun convertListToText(
        value: Any?
    ): String {
        return when (value) {
            is List<*> -> value
                .filterIsInstance<String>()
                .filter { item ->
                    item.isNotBlank()
                }
                .joinToString(", ")

            is String -> value

            else -> ""
        }
    }

    private fun showLoading(
        isLoading: Boolean
    ) {
        progressEditProfile.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        editProfileScrollView.visibility =
            if (isLoading) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }
}