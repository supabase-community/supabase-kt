package io.github.jan.supabase.compose.auth

import kotlinx.serialization.json.JsonObject


open class LoginConfig(open val serverClientId: String)

data class GoogleLoginConfig(
    override val serverClientId: String,
    val isSupported: Boolean = true,
    val filterByAuthorizedAccounts: Boolean = false,
    val associateLinkedAccounts: Pair<String, List<String>>? = null,
    val nonce: String? = null,
    var extraData: JsonObject? = null
) : LoginConfig(serverClientId)

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


data class AppleLoginConfig(
    override val serverClientId: String = "",
    val nonce: String? = null,
    var extraData: JsonObject? = null
) : LoginConfig(serverClientId)


fun ComposeAuth.Config.appleNativeLogin(
    serverClientId: String,
    nonce: String?,
    extraData: JsonObject?
) = AppleLoginConfig(serverClientId, nonce, extraData)