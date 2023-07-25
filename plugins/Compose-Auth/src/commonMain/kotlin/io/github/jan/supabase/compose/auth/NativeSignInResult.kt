package io.github.jan.supabase.compose.auth

sealed interface NativeSignInResult {
    data object Canceled : NativeSignInResult
    data object NetworkError : NativeSignInResult
    data object ClosedByUser: NativeSignInResult
    data object AccountNotFound : NativeSignInResult
    data class Success(val idToken: String) : NativeSignInResult
    data class Error(val message: String) : NativeSignInResult
}