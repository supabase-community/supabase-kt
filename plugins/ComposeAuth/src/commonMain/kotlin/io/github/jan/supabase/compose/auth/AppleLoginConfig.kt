package io.github.jan.supabase.compose.auth

import kotlinx.serialization.json.JsonObject

/**
 * Config for Apple's Authorization API
 * @param nonce A string value to pass to the identity provider.
 * @param extraData Add extra data for user on sign-in
 */
data class AppleLoginConfig(
    val serverClientId: String = "",
    val nonce: String? = null,
    var extraData: JsonObject? = null
)

/**
 * Helper function that return native configs
 */
fun ComposeAuth.Config.appleNativeLogin(
    serverClientId: String = "",
    nonce: String? = null,
    extraData: JsonObject? = null
) {
    appleLoginConfig = AppleLoginConfig(serverClientId, nonce, extraData)
}