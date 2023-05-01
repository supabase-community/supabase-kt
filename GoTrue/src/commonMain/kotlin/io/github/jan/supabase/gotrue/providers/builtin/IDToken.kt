package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.exceptions.SupabaseEncodingException
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
 * Only Apple and Google are supported as providers.
 *
 */
object IDToken : DefaultAuthProvider<IDToken.Config, IDToken.Result> {

    @Serializable(with = DefaultAuthProvider.Config.Companion::class)
    data class Config(@SerialName("id_token") var idToken: String = "", @SerialName("client_id") var clientId: String = "", var provider: IDTokenProvider? = null, var nonce: String? = null): DefaultAuthProvider.Config()

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