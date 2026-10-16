package io.github.jan.supabase.auth.native.external.google

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OAuthProviders
import io.github.jan.supabase.auth.native.external.signInWithOAuth
import io.github.jan.supabase.auth.user.UserSession

expect class GoogleCredential

sealed interface GoogleSignInResult {

    class Success(val session: UserSession, val credential: GoogleCredential): GoogleSignInResult
    data object OAuthInitiated: GoogleSignInResult
    data object Cancelled: GoogleSignInResult

}

expect suspend fun Auth.signWithGoogle(
    config: GoogleSignInConfig.() -> Unit = {}
): GoogleSignInResult

internal suspend fun Auth.signInWithGoogleFallback(config: GoogleSignInConfig.() -> Unit = {}): GoogleSignInResult.OAuthInitiated {
    val config = GoogleSignInConfig("").apply(config)
    signInWithOAuth(OAuthProviders.GOOGLE, config)
    return GoogleSignInResult.OAuthInitiated
}