package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OAuthProviders
import io.github.jan.supabase.auth.native.external.signInWithOAuth
import io.github.jan.supabase.auth.user.UserSession

actual class AppleSignInResult {
    actual val session: UserSession get() = error("No session will be returned upon starting the OAuth. Use `currentSessionOrNull()` after OAuth completed.")
}

actual suspend fun Auth.signInWithApple(config: AppleSignInConfig.() -> Unit): AppleSignInResult {
    val config = AppleSignInConfig("--").apply(config)
    signInWithOAuth(OAuthProviders.APPLE, config)
    return AppleSignInResult()
}