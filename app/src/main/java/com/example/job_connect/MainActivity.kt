package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val createAccountButton =
            findViewById<Button>(R.id.btnCreateAccount)

        val signInButton =
            findViewById<Button>(R.id.btnSignIn)

        createAccountButton.setOnClickListener {
            Log.d(TAG, "Create Account button clicked")

            val registerIntent =
                Intent(this, RegisterActivity::class.java)

            startActivity(registerIntent)
        }

        signInButton.setOnClickListener {
            Log.d(TAG, "Sign In button clicked")

            val loginIntent =
                Intent(this, LoginActivity::class.java)

            startActivity(loginIntent)
        }
    }
}