package io.github.jan.supabase.auth.native.native.oauth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OAuthConfig
import io.github.jan.supabase.auth.providers.OAuthProvider

suspend fun Auth.signInWithOAuth(
    provider: OAuthProvider,
    config: OAuthConfig.() -> Unit = {}
) {

}