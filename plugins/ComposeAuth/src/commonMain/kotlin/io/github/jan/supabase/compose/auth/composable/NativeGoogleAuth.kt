package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.fallbackLogin

/**
 * Enum class for the type of Google Dialog
 */
enum class GoogleDialogType {
    /**
     * A bottom sheet dialog
     */
    BOTTOM_SHEET,
    /**
     * A standard dialog
     */
    DIALOG
}

/**
 * Composable function that implements Native Google Auth.
 *
 * On unsupported platforms it will use the [fallback]
 *
 * @param onResult Callback for the result of the login
 * @param fallback Fallback function for unsupported platforms
 * @return [NativeSignInState]
 */
@Composable
expect fun ComposeAuth.rememberSignInWithGoogle(
    onResult: (NativeSignInResult) -> Unit = {},
    type: GoogleDialogType = GoogleDialogType.DIALOG,
    fallback: suspend () -> Unit = {
        fallbackLogin(Google)
    }
): NativeSignInState

internal expect suspend fun handleGoogleSignOut()