package io.github.jan.supabase.compose.auth.ui.email

fun interface EmailValidator {

    fun validate(email: String): Boolean

    companion object {

        private val emailRegex = Regex("^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,}\$")

        val REGEX = EmailValidator {
            emailRegex.matches(it)
        }

    }

}