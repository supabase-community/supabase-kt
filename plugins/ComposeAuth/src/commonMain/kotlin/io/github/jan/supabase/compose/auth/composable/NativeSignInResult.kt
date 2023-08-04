package io.github.jan.supabase.compose.auth.composable


/**
 * Response of Native SignIn
 */
sealed interface NativeSignInResult {

    /**
     *
     * On Successful SignIn
     *
     */
    data object Success : NativeSignInResult

    /**
     *
     * User canceled or clicked away
     *
     */
    data object ClosedByUser : NativeSignInResult

    /**
     *
     * Network occurred error
     *
     * @param message
     *
     */
    data class NetworkError(val message: String) : NativeSignInResult

    /**
     *
     *  Exceptions or inner error occurred
     *
     *  @param message
     *
     */
    data class Error(val message: String) : NativeSignInResult
}