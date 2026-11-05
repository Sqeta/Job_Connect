package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG =
            "MainActivity"

        private const val FCM_TAG =
            "JOB_CONNECT_FCM_TOKEN"
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrieveFirebaseToken()
        setupButtons()
    }

    private fun setupButtons() {
        val createAccountButton =
            findViewById<Button>(
                R.id.btnCreateAccount
            )

        val signInButton =
            findViewById<Button>(
                R.id.btnSignIn
            )

        createAccountButton.setOnClickListener {
            Log.d(
                TAG,
                "Create Account button clicked"
            )

            val registerIntent =
                Intent(
                    this,
                    RegisterActivity::class.java
                )

            startActivity(registerIntent)
        }

        signInButton.setOnClickListener {
            Log.d(
                TAG,
                "Sign In button clicked"
            )

            val loginIntent =
                Intent(
                    this,
                    LoginActivity::class.java
                )

            startActivity(loginIntent)
        }
    }

    private fun retrieveFirebaseToken() {
        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.e(
                        FCM_TAG,
                        "Failed to retrieve Firebase token",
                        task.exception
                    )

                    return@addOnCompleteListener
                }

                val token =
                    task.result

                Log.d(
                    FCM_TAG,
                    "FCM TOKEN: $token"
                )

                saveTokenForSignedInUser(token)
            }
    }

    private fun saveTokenForSignedInUser(
        token: String
    ) {
        val userId =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

        if (userId == null) {
            Log.d(
                FCM_TAG,
                "Token retrieved, but user is not signed in"
            )

            return
        }

        val tokenData =
            hashMapOf<String, Any>(
                "fcmToken" to token,
                "tokenUpdatedAt" to
                        FieldValue.serverTimestamp()
            )

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(userId)
            .set(
                tokenData,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                Log.d(
                    FCM_TAG,
                    "FCM token saved to Firestore"
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    FCM_TAG,
                    "Failed to save FCM token",
                    exception
                )
            }
    }
}