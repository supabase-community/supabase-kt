package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OAuthProviders
import io.github.jan.supabase.auth.native.external.signInWithOAuth
import io.github.jan.supabase.auth.user.UserSession

expect class AppleCredential

sealed interface AppleSignInResult {

    class Success(val session: UserSession, val credential: AppleCredential): AppleSignInResult
    data object OAuthInitiated: AppleSignInResult
    data object Cancelled: AppleSignInResult

}

expect suspend fun Auth.signInWithApple(
    config: AppleSignInConfig.() -> Unit = {}
): AppleSignInResult

internal suspend fun Auth.signInWithAppleFallback(config: AppleSignInConfig.() -> Unit): AppleSignInResult {
    val config = AppleSignInConfig("--").apply(config)
    signInWithOAuth(OAuthProviders.APPLE, config)
    return AppleSignInResult.OAuthInitiated
}