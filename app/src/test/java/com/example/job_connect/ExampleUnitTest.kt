package com.example.job_connect

import com.example.job_connect.util.InputValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit tests for JobConnect input validation.
 *
 * These tests verify valid inputs, invalid inputs, boundary values,
 * whitespace handling and comma-separated profile information.
 */
class ExampleUnitTest {

    @Test
    fun fullName_withValidName_returnsTrue() {
        val result =
            InputValidator.isValidFullName(
                "Karabo Mohapi"
            )

        assertTrue(result)
    }

    @Test
    fun fullName_withOneCharacter_returnsFalse() {
        val result =
            InputValidator.isValidFullName("K")

        assertFalse(result)
    }

    @Test
    fun fullName_withOnlySpaces_returnsFalse() {
        val result =
            InputValidator.isValidFullName("   ")

        assertFalse(result)
    }

    @Test
    fun email_withValidAddress_returnsTrue() {
        val result =
            InputValidator.isValidEmail(
                "karabo@example.com"
            )

        assertTrue(result)
    }

    @Test
    fun email_withoutAtSymbol_returnsFalse() {
        val result =
            InputValidator.isValidEmail(
                "karaboexample.com"
            )

        assertFalse(result)
    }

    @Test
    fun email_withoutDomainExtension_returnsFalse() {
        val result =
            InputValidator.isValidEmail(
                "karabo@example"
            )

        assertFalse(result)
    }

    @Test
    fun password_withSixCharacters_returnsTrue() {
        val result =
            InputValidator.isValidPassword(
                "123456"
            )

        assertTrue(result)
    }

    @Test
    fun password_withLessThanSixCharacters_returnsFalse() {
        val result =
            InputValidator.isValidPassword(
                "12345"
            )

        assertFalse(result)
    }

    @Test
    fun searchKeyword_withValidText_returnsTrue() {
        val result =
            InputValidator.isValidSearchKeyword(
                "Android Developer"
            )

        assertTrue(result)
    }

    @Test
    fun searchKeyword_withOnlySpaces_returnsFalse() {
        val result =
            InputValidator.isValidSearchKeyword(
                "    "
            )

        assertFalse(result)
    }

    @Test
    fun location_withValidCity_returnsTrue() {
        val result =
            InputValidator.isValidLocation(
                "Johannesburg"
            )

        assertTrue(result)
    }

    @Test
    fun location_withEmptyText_returnsFalse() {
        val result =
            InputValidator.isValidLocation("")

        assertFalse(result)
    }

    @Test
    fun commaSeparatedText_convertsToCleanList() {
        val result =
            InputValidator.convertCommaSeparatedText(
                "Kotlin, Java, SQL"
            )

        val expected =
            listOf(
                "Kotlin",
                "Java",
                "SQL"
            )

        assertEquals(expected, result)
    }

    @Test
    fun commaSeparatedText_removesBlankItems() {
        val result =
            InputValidator.convertCommaSeparatedText(
                "Kotlin, , Java,   , SQL"
            )

        val expected =
            listOf(
                "Kotlin",
                "Java",
                "SQL"
            )

        assertEquals(expected, result)
    }

    @Test
    fun commaSeparatedText_removesDuplicates() {
        val result =
            InputValidator.convertCommaSeparatedText(
                "Kotlin, Java, Kotlin"
            )

        val expected =
            listOf(
                "Kotlin",
                "Java"
            )

        assertEquals(expected, result)
    }

    @Test
    fun commaSeparatedText_withEmptyInput_returnsEmptyList() {
        val result =
            InputValidator.convertCommaSeparatedText("")

        assertTrue(result.isEmpty())
    }
}