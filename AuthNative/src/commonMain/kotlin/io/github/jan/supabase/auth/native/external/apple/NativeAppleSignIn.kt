package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.DefaultIdTokenConfig
import io.github.jan.supabase.auth.DefaultOAuthConfig
import io.github.jan.supabase.auth.IdTokenConfig
import io.github.jan.supabase.auth.OAuthConfig
import io.github.jan.supabase.auth.OAuthProviders
import io.github.jan.supabase.auth.user.UserSession

expect class AppleSignInResult {

    val session: UserSession

}

class AppleSignInConfig(token: String):
    IdTokenConfig by DefaultIdTokenConfig(OAuthProviders.APPLE, token),
    OAuthConfig by DefaultOAuthConfig()

expect suspend fun Auth.signInWithApple(
    config: AppleSignInConfig.() -> Unit = {}
): AppleSignInResult