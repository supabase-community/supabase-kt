package io.github.jan.supabase.compose.auth.ui

/**
 * FormValidator interface used to validate text fields.
 */
fun interface FormValidator {

    /**
     * Validates the given [value].
     */
    fun validate(value: String): Boolean

    companion object {

        private val emailRegex = Regex("^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,}\$")

        /**
         * Validates the given [value] as an email, using REGEX.
         */
        val EMAIL = FormValidator {
            emailRegex.matches(it)
        }

        /**
         * Validates the given [value] as a phone number, by verifying that all characters are digits.
         */
        val PHONE = FormValidator {
            it.all { char -> char.isDigit() }
        }

    }

}