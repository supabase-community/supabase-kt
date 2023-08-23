package io.github.jan.supabase.compose.auth

import kotlinx.serialization.json.JsonObject

/**
 * Config for [ComposeAuth]
 */
interface LoginConfig {
    /**
     * Returns clientId for native login
     */
    val serverClientId: String
}

/**
 * Config for requesting IDToken from play-auth API
 * @param isSupported Sets whether Google ID token-backed credentials should be returned by the API.
 * @param filterByAuthorizedAccounts Sets whether to only allow the user to select from Google accounts that are already authorized to sign in to your application.
 * @param associateLinkedAccounts Sets whether to support sign-in using Google accounts that are linked to your users' accounts.
 * @param nonce Sets the nonce to use when generating a Google ID token.
 * @param extraData Add extra data for user on sign-in
 */
data class GoogleLoginConfig(
    override val serverClientId: String,
    val isSupported: Boolean = true,
    val filterByAuthorizedAccounts: Boolean = false,
    val associateLinkedAccounts: Pair<String, List<String>>? = null,
    val nonce: String? = null,
    var extraData: JsonObject? = null
) : LoginConfig

/**
 * Config for Apple's Authorization API
 * @param nonce A string value to pass to the identity provider.
 * @param extraData Add extra data for user on sign-in
 */
data class AppleLoginConfig(
    override val serverClientId: String = "",
    val nonce: String? = null,
    var extraData: JsonObject? = null
) : LoginConfig

/**
 * Helper function that return native configs
 */
fun ComposeAuth.Config.googleNativeLogin(
    serverClientId: String,
    isSupported: Boolean = true,
    filterByAuthorizedAccounts: Boolean = false,
    associateLinkedAccounts: Pair<String, List<String>>? = null,
    nonce: String? = null,
    extraData: JsonObject? = null
) {
    loginConfig["google"] =
        GoogleLoginConfig(
            serverClientId,
            isSupported,
            filterByAuthorizedAccounts,
            associateLinkedAccounts,
            nonce,
            extraData
        )
}

/**
 * Helper function that return native configs
 */
fun ComposeAuth.Config.appleNativeLogin(
    serverClientId: String = "",
    nonce: String? = null,
    extraData: JsonObject? = null
) {
    loginConfig["apple"] = AppleLoginConfig(serverClientId, nonce, extraData)
}