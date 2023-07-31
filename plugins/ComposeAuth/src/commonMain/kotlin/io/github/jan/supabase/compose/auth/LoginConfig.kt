package io.github.jan.supabase.compose.auth


open class LoginConfig(open val serverClientId: String) {

    /**
     * @param key
     * @param value
     * json data saved to table "raw_users_meta_data"
     */

    abstract class ExtraData(open val key: String, open var value: String? = null)
}

data class GoogleLoginConfig(
    override val serverClientId: String,
    val isSupported: Boolean = true,
    val filterByAuthorizedAccounts: Boolean = false,
    val associateLinkedAccounts: Pair<String, List<String>>? = null,
    val nonce: String? = null,
    val extraData: List<ExtraData>? = null
) : LoginConfig(serverClientId) {

    data class ID(override val key: String) : ExtraData(key)
    data class DisplayName(override val key: String) : ExtraData(key)
    data class FamilyName(override val key: String) : ExtraData(key)
    data class GivenName(override val key: String) : ExtraData(key)
    data class ProfilePic(override val key: String) : ExtraData(key)
}

fun ComposeAuth.Config.googleNativeLogin(
    serverClientId: String,
    isSupported: Boolean = true,
    filterByAuthorizedAccounts: Boolean = false,
    associateLinkedAccounts: Pair<String, List<String>>? = null,
    nonce: String? = null,
    extraData: List<LoginConfig.ExtraData>? = null
) = GoogleLoginConfig(
    serverClientId,
    isSupported,
    filterByAuthorizedAccounts,
    associateLinkedAccounts,
    nonce,
    extraData
)
