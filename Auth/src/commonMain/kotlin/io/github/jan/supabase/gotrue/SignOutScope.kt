package io.github.jan.supabase.gotrue

/**
 * Represents the scope of a sign-out action.
 *
 * The sign-out scope determines the scope of the sign-out action being performed.
 *
 * @property GLOBAL Sign-out action applies to all sessions across the entire system.
 * @property LOCAL Sign-out action applies only to the current session.
 * @property OTHERS Sign-out action applies to other sessions, excluding the current session.
 */
enum class SignOutScope {
    GLOBAL, LOCAL, OTHERS
}