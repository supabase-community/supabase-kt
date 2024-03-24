package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.exceptions.SupabaseEncodingException
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.Azure
import io.github.jan.supabase.gotrue.providers.Facebook
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.IDTokenProvider
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Allows signing in with an OIDC ID token. The authentication provider used should be enabled and configured.
 *
 * Only [Apple], [Google], [Facebook] and [Azure] are supported as providers.
 *
 */
data object IDToken : DefaultAuthProvider<IDToken.Config, UserInfo> {

    override val grantType: String = "id_token"

    /**
     * The configuration for the id token authentication method
     * @param idToken OIDC ID token issued by the specified provider. The `iss` claim in the ID token must match the supplied provider. Some ID tokens contain an `at_hash` which require that you provide an `access_token` value to be accepted properly. If the token contains a `nonce` claim you must supply the nonce used to obtain the ID token.
     * @param provider The provider of the id token. Only [Apple], [Google], [Facebook] and [Azure] are supported
     * @param accessToken If the ID token contains an `at_hash` claim, then the hash of this value is compared to the value in the ID token.
     * @param nonce If the ID token contains a `nonce` claim, then the hash of this value is compared to the value in the ID token.
     */
    @Serializable
    data class Config(
        @SerialName("id_token") var idToken: String = "",
        var provider: IDTokenProvider? = null,
        @SerialName("access_token") var accessToken: String? = null,
        var nonce: String? = null
    ) : DefaultAuthProvider.Config()

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeResult(json: JsonObject): UserInfo = try {
        supabaseJson.decodeFromJsonElement(json)
    } catch(e: MissingFieldException) {
        throw SupabaseEncodingException("Couldn't decode sign up id token result. Input: $json")
    }

    override fun encodeCredentials(credentials: Config.() -> Unit): JsonObject = supabaseJson.encodeToJsonElement(Config().apply(credentials)).jsonObject

}