package io.github.jan.supabase.compose.auth.ui

fun interface FormValidator {

    fun validate(value: String): Boolean

    companion object {

        private val emailRegex = Regex("^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,}\$")

        val EMAIL = FormValidator {
            emailRegex.matches(it)
        }

        val PHONE = FormValidator {
            it.all { char -> char.isDigit() }
        }

    }

}