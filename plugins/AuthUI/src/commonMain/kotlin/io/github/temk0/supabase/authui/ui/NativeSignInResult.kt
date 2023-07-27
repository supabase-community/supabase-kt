package io.github.temk0.supabase.authui.ui

sealed interface NativeSignInResult {
    data object Success : NativeSignInResult
    data class Error(val message: String) : NativeSignInResult
    data class NetworkError(val message: String) : NativeSignInResult
    data object ClosedByUser : NativeSignInResult
}