package io.github.jan.supabase.compose.auth.ui.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Represents a rule that can be applied to a password.
 * @param description The description of the rule.
 * @param predicate The predicate that determines whether the rule is fulfilled.
 */
class PasswordRule(val description: String, val predicate: (password: String) -> Boolean) {

    companion object {

        /**
         * Creates a [PasswordRule] that checks whether the password is at least [length] characters long.
         * @param length The minimum length of the password.
         * @param description The description of the rule.
         */
        fun minLength(length: Int, description: String = "Password must be at least $length characters long") = PasswordRule(description) {
            it.length >= length
        }

        /**
         * Creates a [PasswordRule] that checks whether the password contains at least one lowercase character.
         * @param description The description of the rule.
         */
        fun containsLowercase(description: String = "Password must contain at least one lowercase character") = PasswordRule(description) {
            it.any { char -> char.isLowerCase() }
        }

        /**
         * Creates a [PasswordRule] that checks whether the password contains at least one uppercase character.
         */
        fun containsUppercase(description: String = "Password must contain at least one uppercase character") = PasswordRule(description) {
            it.any { char -> char.isUpperCase() }
        }

        /**
         * Creates a [PasswordRule] that checks whether the password contains at least one digit.
         */
        fun containsDigit(description: String = "Password must contain at least one digit") = PasswordRule(description) {
            it.any { char -> char.isDigit() }
        }

        /**
         * Creates a [PasswordRule] that checks whether the password contains at least one special character.
         */
        fun containsSpecialCharacter(description: String = "Password must contain at least one special character") = PasswordRule(description) {
            it.any { char -> char.isLetterOrDigit().not() }
        }

        /**
         * Creates a [PasswordRule] that checks whether the password is at most [length] characters long.
         */
        fun maxLength(length: Int, description: String = "Password must be at most $length characters long") = PasswordRule(description) {
            it.length <= length
        }

    }

}

/**
 * Represents the result of a [PasswordRule], when applied to a password.
 * @param description The description of the rule.
 * @param isFulfilled Whether the rule is fulfilled.
 */
data class PasswordRuleResult(val description: String, val isFulfilled: Boolean)

@Composable
fun rememberPasswordRuleList(vararg rules: PasswordRule) = remember { rules.toList() }