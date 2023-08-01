package io.github.jan.supabase.compose.auth.composable

sealed interface NativeSignInResult {
    data class Success(val idToken: String? = null) : NativeSignInResult
    data class Error(val message: String) : NativeSignInResult
    data class NetworkError(val message: String) : NativeSignInResult
    data object ClosedByUser : NativeSignInResult
}