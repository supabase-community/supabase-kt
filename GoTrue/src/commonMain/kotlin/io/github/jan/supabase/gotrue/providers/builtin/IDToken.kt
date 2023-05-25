package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.exceptions.SupabaseEncodingException
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.IDTokenProvider
import io.github.jan.supabase.supabaseJson
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Authentication method with id token, client id, provider and optionally nonce.
 *
 * Only [Apple] and [Google] are supported as providers.
 *
 */
object IDToken : DefaultAuthProvider<IDToken.Config, IDToken.Result> {

    override val grantType: String = "id_token"

    /**
     * The configuration for the id token authentication method
     * @param idToken The id token received from the [provider]
     * @param clientId The oauth client id of the app
     * @param provider The provider of the id token. Only [Apple] and [Google] are supported
     * @param nonce The nonce used to verify the id token
     */
    @Serializable
    data class Config(
        @SerialName("id_token") var idToken: String = "",
        @SerialName("client_id") var clientId: String = "",
        var provider: IDTokenProvider? = null,
        var nonce: String? = null
    ) : DefaultAuthProvider.Config()

    /**
     * The sign up result of the id token authentication method
     * @param id The id of the created user
     * @param confirmationSentAt The time the confirmation was sent
     * @param createdAt The time the user was created
     * @param updatedAt The time the user was updated
     */
    @Serializable
    data class Result(
        val id: String,
        @SerialName("confirmation_sent_at") val confirmationSentAt: Instant,
        @SerialName("created_at") val createdAt: Instant,
        @SerialName("updated_at") val updatedAt: Instant,
    )

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeResult(json: JsonObject): Result = try {
        supabaseJson.decodeFromJsonElement(json)
    } catch(e: MissingFieldException) {
        throw SupabaseEncodingException("Couldn't decode sign up id token result. Input: $json")
    }

    override fun encodeCredentials(credentials: Config.() -> Unit): JsonObject = supabaseJson.encodeToJsonElement(Config().apply(credentials)).jsonObject

}