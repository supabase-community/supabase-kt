package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.compose.auth.composable.handleGoogleSignOut

/**
 * Config for requesting IDToken from play-auth API
 * @param isSupported Sets whether Google ID token-backed credentials should be returned by the API.
 * @param associateLinkedAccounts Sets whether to support sign-in using Google accounts that are linked to your users' accounts.
 * @param handleSignOut Sets whether to sign out from Google on sign out from your app
 */
data class GoogleLoginConfig(
    val serverClientId: String,
    val isSupported: Boolean = true,
    val associateLinkedAccounts: Pair<String, List<String>>? = null,
    val handleSignOut: (suspend () -> Unit)? = ::handleGoogleSignOut
)

/**
 * Helper function that return native configs
 * @param serverClientId The server client ID
 * @param isSupported Sets whether Google ID token-backed credentials should be returned by the API.
 * @param associateLinkedAccounts Sets whether to support sign-in using Google accounts that are linked to your users' accounts.
 * @param handleSignOut Sets whether to sign out from Google on sign out from your app
 */
fun ComposeAuth.Config.googleNativeLogin(
    serverClientId: String,
    isSupported: Boolean = true,
    associateLinkedAccounts: Pair<String, List<String>>? = null,
    handleSignOut: (suspend () -> Unit)? = ::handleGoogleSignOut
) {
    googleLoginConfig =
        GoogleLoginConfig(
            serverClientId,
            isSupported,
            associateLinkedAccounts,
            handleSignOut
        )
}