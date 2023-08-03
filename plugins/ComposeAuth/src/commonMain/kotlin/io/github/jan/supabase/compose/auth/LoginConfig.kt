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
 */
data class AppleLoginConfig(
    override val serverClientId: String = "",
    val nonce: String? = null,
    var extraData: JsonObject? = null
) : LoginConfig

/**
 * Helper functions that return native configs
 */
fun ComposeAuth.Config.googleNativeLogin(
    serverClientId: String,
    isSupported: Boolean = true,
    filterByAuthorizedAccounts: Boolean = false,
    associateLinkedAccounts: Pair<String, List<String>>? = null,
    nonce: String? = null,
    extraData: JsonObject? = null
) = GoogleLoginConfig(
    serverClientId,
    isSupported,
    filterByAuthorizedAccounts,
    associateLinkedAccounts,
    nonce,
    extraData
)

fun ComposeAuth.Config.appleNativeLogin(
    serverClientId: String = "",
    nonce: String? = null,
    extraData: JsonObject? = null
) = AppleLoginConfig(serverClientId, nonce, extraData)