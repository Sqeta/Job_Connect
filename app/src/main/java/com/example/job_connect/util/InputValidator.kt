package com.example.job_connect.util

/**
 * Contains reusable validation functions used throughout JobConnect.
 *
 * These functions do not depend on Android components, which makes
 * them suitable for fast local unit testing.
 */
object InputValidator {

    private val emailPattern =
        Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )

    fun isValidFullName(
        fullName: String
    ): Boolean {
        return fullName.trim().length >= 2
    }

    fun isValidEmail(
        email: String
    ): Boolean {
        return emailPattern.matches(
            email.trim()
        )
    }

    fun isValidPassword(
        password: String
    ): Boolean {
        return password.length >= 6
    }

    fun isValidSearchKeyword(
        keyword: String
    ): Boolean {
        return keyword.trim().isNotEmpty()
    }

    fun isValidLocation(
        location: String
    ): Boolean {
        return location.trim().isNotEmpty()
    }

    fun convertCommaSeparatedText(
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
}