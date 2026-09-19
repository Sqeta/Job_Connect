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

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        val emailInput =
            findViewById<EditText>(R.id.etLoginEmail)

        val passwordInput =
            findViewById<EditText>(R.id.etLoginPassword)

        val loginButton =
            findViewById<Button>(R.id.btnLogin)

        val forgotPasswordText =
            findViewById<TextView>(R.id.tvForgotPassword)

        val registerText =
            findViewById<TextView>(R.id.tvGoToRegister)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            when {
                email.isEmpty() -> {
                    emailInput.error = "Please enter your email address"
                    emailInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Please enter a valid email address"
                    emailInput.requestFocus()
                }

                password.isEmpty() -> {
                    passwordInput.error = "Please enter your password"
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
                        password = password,
                        loginButton = loginButton
                    )
                }
            }
        }

        forgotPasswordText.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error =
                    "Enter your email address before requesting a reset"

                emailInput.requestFocus()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Please enter a valid email address"
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

    private fun signInUser(
        email: String,
        password: String,
        loginButton: Button
    ) {
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."

        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase login successful")

                    Toast.makeText(
                        this,
                        "Welcome to JobConnect",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, HomeActivity::class.java)

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                } else {
                    Log.e(
                        TAG,
                        "Firebase login failed",
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

    private fun sendPasswordReset(email: String) {
        firebaseAuth
            .sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Password-reset email sent")

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
}