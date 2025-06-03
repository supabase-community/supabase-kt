package io.github.jan.supabase.compose.auth.composable

/**
 * Represents the response of a Native SignIn
 */
sealed interface NativeSignInResult {

    /**
     * User successfully signed in
     */
    data object Success : NativeSignInResult

    /**
     * User closed the login dialog
     */
    data object ClosedByUser : NativeSignInResult

    /**
     * Network error occurred
     * @property message The error message
     */
    @Deprecated
    data class NetworkError(val message: String) : NativeSignInResult

    /**
     * Error occurred
     * @property message The error message
     * @property exception The exception that caused the error
     */
    data class Error(val message: String, val exception: Exception? = null) : NativeSignInResult
}