package io.github.jan.supabase.gotrue

/**
 * Represents the scope of a logout action.
 *
 * The logout scope determines the scope of the logout action being performed.
 *
 * @property GLOBAL Logout action applies to all sessions across the entire system.
 * @property LOCAL Logout action applies only to the current session.
 * @property OTHERS Logout action applies to other sessions, excluding the current session.
 */
enum class LogoutScope {
    GLOBAL, LOCAL, OTHERS
}