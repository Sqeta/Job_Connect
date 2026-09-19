package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val fullNameInput =
            findViewById<EditText>(R.id.etFullName)

        val emailInput =
            findViewById<EditText>(R.id.etEmail)

        val passwordInput =
            findViewById<EditText>(R.id.etPassword)

        val confirmPasswordInput =
            findViewById<EditText>(R.id.etConfirmPassword)

        val registerButton =
            findViewById<Button>(R.id.btnRegister)

        val loginText =
            findViewById<TextView>(R.id.tvGoToLogin)

        registerButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when {
                fullName.isEmpty() -> {
                    fullNameInput.error = "Please enter your full name"
                    fullNameInput.requestFocus()
                }

                email.isEmpty() -> {
                    emailInput.error = "Please enter your email address"
                    emailInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Please enter a valid email address"
                    emailInput.requestFocus()
                }

                password.length < 6 -> {
                    passwordInput.error =
                        "Password must contain at least 6 characters"

                    passwordInput.requestFocus()
                }

                password != confirmPassword -> {
                    confirmPasswordInput.error = "Passwords do not match"
                    confirmPasswordInput.requestFocus()
                }

                else -> {
                    createFirebaseAccount(
                        fullName = fullName,
                        email = email,
                        password = password,
                        registerButton = registerButton
                    )
                }
            }
        }

        loginText.setOnClickListener {
            Log.d(TAG, "Sign-in link selected")

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
    }

    private fun createFirebaseAccount(
        fullName: String,
        email: String,
        password: String,
        registerButton: Button
    ) {
        registerButton.isEnabled = false
        registerButton.text = "Creating account..."

        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase account created successfully")

                    val firebaseUser = firebaseAuth.currentUser

                    if (firebaseUser == null) {
                        restoreRegisterButton(registerButton)
                        showMessage("Account could not be loaded")
                        return@addOnCompleteListener
                    }

                    val profileUpdate =
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                    firebaseUser
                        .updateProfile(profileUpdate)
                        .addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Log.d(TAG, "Authentication profile updated")
                            } else {
                                Log.w(
                                    TAG,
                                    "Display name could not be updated",
                                    profileTask.exception
                                )
                            }

                            saveProfileToFirestore(
                                userId = firebaseUser.uid,
                                fullName = fullName,
                                email = email,
                                registerButton = registerButton
                            )
                        }
                } else {
                    Log.e(
                        TAG,
                        "Firebase registration failed",
                        task.exception
                    )

                    restoreRegisterButton(registerButton)

                    val errorMessage =
                        task.exception?.localizedMessage
                            ?: "Account registration failed"

                    showMessage(errorMessage)
                }
            }
    }

    private fun saveProfileToFirestore(
        userId: String,
        fullName: String,
        email: String,
        registerButton: Button
    ) {
        val userProfile = hashMapOf(
            "userId" to userId,
            "fullName" to fullName,
            "email" to email,
            "education" to "",
            "skills" to emptyList<String>(),
            "preferredLocations" to emptyList<String>(),
            "preferredJobTypes" to emptyList<String>(),
            "language" to "en",
            "notificationsEnabled" to true,
            "profileCompleted" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore
            .collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Log.d(TAG, "Firestore user profile created")

                completeRegistration()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Firestore profile creation failed",
                    exception
                )

                restoreRegisterButton(registerButton)

                showMessage(
                    "Account created, but the profile could not be saved. " +
                            "Please try signing in."
                )

                firebaseAuth.signOut()

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )

                finish()
            }
    }

    private fun restoreRegisterButton(registerButton: Button) {
        registerButton.isEnabled = true
        registerButton.text = "Create account"
    }

    private fun completeRegistration() {
        showMessage(
            "Account and profile created successfully. Please sign in."
        )

        firebaseAuth.signOut()

        val intent = Intent(this, LoginActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}