package io.github.jan.supabase.compose.auth.ui.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class PasswordRule(val description: String, val predicate: (password: String) -> Boolean) {

    companion object {

        fun minLength(length: Int, description: String = "Password must be at least $length characters long") = PasswordRule(description) {
            it.length >= length
        }

        fun containsLowercase(description: String = "Password must contain at least one lowercase character") = PasswordRule(description) {
            it.any { char -> char.isLowerCase() }
        }

        fun containsUppercase(description: String = "Password must contain at least one uppercase character") = PasswordRule(description) {
            it.any { char -> char.isUpperCase() }
        }

        fun containsDigit(description: String = "Password must contain at least one digit") = PasswordRule(description) {
            it.any { char -> char.isDigit() }
        }

        fun containsSpecialCharacter(description: String = "Password must contain at least one special character") = PasswordRule(description) {
            it.any { char -> char.isLetterOrDigit().not() }
        }

        fun maxLength(length: Int, description: String = "Password must be at most $length characters long") = PasswordRule(description) {
            it.length <= length
        }

    }

}

data class PasswordRuleResult(val description: String, val isFulfilled: Boolean)

@Composable
fun rememberPasswordRuleList(vararg rules: PasswordRule) = remember { rules.toList() }