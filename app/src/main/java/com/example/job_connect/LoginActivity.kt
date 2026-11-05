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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager

    private lateinit var loginButton: Button
    private lateinit var googleSignInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        val emailInput =
            findViewById<EditText>(R.id.etLoginEmail)

        val passwordInput =
            findViewById<EditText>(R.id.etLoginPassword)

        loginButton =
            findViewById(R.id.btnLogin)

        googleSignInButton =
            findViewById(R.id.btnGoogleSignIn)

        val forgotPasswordText =
            findViewById<TextView>(R.id.tvForgotPassword)

        val registerText =
            findViewById<TextView>(R.id.tvGoToRegister)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            when {
                email.isEmpty() -> {
                    emailInput.error =
                        "Please enter your email address"

                    emailInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches() -> {

                    emailInput.error =
                        "Please enter a valid email address"

                    emailInput.requestFocus()
                }

                password.isEmpty() -> {
                    passwordInput.error =
                        "Please enter your password"

                    passwordInput.requestFocus()
                }

                password.length < 6 -> {
                    passwordInput.error =
                        "Password must contain at least 6 characters"

                    passwordInput.requestFocus()
                }

                else -> {
                    signInUser(
                        email = email,
                        password = password
                    )
                }
            }
        }

        googleSignInButton.setOnClickListener {
            startGoogleSignIn()
        }

        forgotPasswordText.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error =
                    "Enter your email address before requesting a reset"

                emailInput.requestFocus()
            } else if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {
                emailInput.error =
                    "Please enter a valid email address"

                emailInput.requestFocus()
            } else {
                sendPasswordReset(email)
            }
        }

        registerText.setOnClickListener {
            Log.d(TAG, "Create Account link selected")

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )

            finish()
        }
    }

    private fun startGoogleSignIn() {
        setGoogleLoading(true)

        val googleSignInOption =
            GetSignInWithGoogleOption.Builder(
                serverClientId = getString(
                    R.string.default_web_client_id
                )
            )
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleSignInOption)
                .build()

        lifecycleScope.launch {
            try {
                val result =
                    credentialManager.getCredential(
                        context = this@LoginActivity,
                        request = request
                    )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential
                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential =
                        GoogleIdTokenCredential
                            .createFrom(credential.data)

                    firebaseAuthWithGoogle(
                        googleCredential.idToken
                    )
                } else {
                    setGoogleLoading(false)

                    Toast.makeText(
                        this@LoginActivity,
                        "Unable to read the selected Google account.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (exception: GetCredentialException) {
                setGoogleLoading(false)

                Log.e(
                    TAG,
                    "Google credential request failed",
                    exception
                )

                Toast.makeText(
                    this@LoginActivity,
                    "Google sign-in was cancelled or unavailable.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: Exception) {
                setGoogleLoading(false)

                Log.e(
                    TAG,
                    "Unexpected Google sign-in error",
                    exception
                )

                Toast.makeText(
                    this@LoginActivity,
                    exception.localizedMessage
                        ?: "Google sign-in failed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        googleIdToken: String
    ) {
        val firebaseCredential =
            GoogleAuthProvider.getCredential(
                googleIdToken,
                null
            )

        firebaseAuth
            .signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        "Firebase Google authentication successful"
                    )

                    saveGoogleUser()
                } else {
                    setGoogleLoading(false)

                    Log.e(
                        TAG,
                        "Firebase Google authentication failed",
                        task.exception
                    )

                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage
                            ?: "Google sign-in failed.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveGoogleUser() {
        val user = firebaseAuth.currentUser

        if (user == null) {
            setGoogleLoading(false)

            Toast.makeText(
                this,
                "Unable to load your Google account.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val userData = hashMapOf(
            "uid" to user.uid,
            "fullName" to (
                    user.displayName ?: "JobConnect User"
                    ),
            "email" to (
                    user.email ?: ""
                    ),
            "profileImageUrl" to (
                    user.photoUrl?.toString() ?: ""
                    ),
            "authenticationProvider" to "Google",
            "lastLoginAt" to FieldValue.serverTimestamp()
        )

        firestore
            .collection("users")
            .document(user.uid)
            .set(
                userData,
                com.google.firebase.firestore.SetOptions.merge()
            )
            .addOnSuccessListener {
                setGoogleLoading(false)

                Toast.makeText(
                    this,
                    "Welcome to JobConnect",
                    Toast.LENGTH_SHORT
                ).show()

                openHomeActivity()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Could not save Google user profile",
                    exception
                )

                setGoogleLoading(false)

                // Authentication succeeded, so the user may continue.
                openHomeActivity()
            }
    }

    private fun signInUser(
        email: String,
        password: String
    ) {
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."

        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        "Firebase email login successful"
                    )

                    Toast.makeText(
                        this,
                        "Welcome to JobConnect",
                        Toast.LENGTH_SHORT
                    ).show()

                    openHomeActivity()
                } else {
                    Log.e(
                        TAG,
                        "Firebase email login failed",
                        task.exception
                    )

                    loginButton.isEnabled = true
                    loginButton.text = "Sign in"

                    Toast.makeText(
                        this,
                        "Incorrect email or password",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun openHomeActivity() {
        val intent =
            Intent(this, HomeActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun sendPasswordReset(email: String) {
        firebaseAuth
            .sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        "Password-reset email sent"
                    )

                    Toast.makeText(
                        this,
                        "Password-reset email sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.e(
                        TAG,
                        "Password-reset request failed",
                        task.exception
                    )

                    val errorMessage =
                        task.exception?.localizedMessage
                            ?: "Unable to send reset email"

                    Toast.makeText(
                        this,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun setGoogleLoading(
        isLoading: Boolean
    ) {
        googleSignInButton.isEnabled = !isLoading

        googleSignInButton.text =
            if (isLoading) {
                "Connecting to Google..."
            } else {
                "Continue with Google"
            }
    }
}