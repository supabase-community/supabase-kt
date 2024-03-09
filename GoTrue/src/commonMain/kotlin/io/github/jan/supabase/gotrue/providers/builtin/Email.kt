package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.exceptions.SupabaseEncodingException
import io.github.jan.supabase.gotrue.user.Identity
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
 * Authentication method with email and password
 */
data object Email : DefaultAuthProvider<Email.Config, Email.Result> {

    override val grantType: String = "password"

    /**
     * The configuration for the email authentication method
     * @param email The email of the user
     * @param password The password of the user
     */
    @Serializable
    data class Config(var email: String = "", var password: String = ""): DefaultAuthProvider.Config()

    /**
     * The sign up result of the email authentication method
     * @param id The id of the created user
     * @param email The email of the created user
     * @param identities The identities of the created user
     * @param confirmationSentAt The time the confirmation was sent
     * @param createdAt The time the user was created
     * @param updatedAt The time the user was updated
     */
    @Serializable
    data class Result(
        val id: String,
        val email: String,
        val identities: List<Identity>?,
        @SerialName("confirmation_sent_at") val confirmationSentAt: Instant,
        @SerialName("created_at") val createdAt: Instant,
        @SerialName("updated_at") val updatedAt: Instant,
    )

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeResult(json: JsonObject): Result = try {
        supabaseJson.decodeFromJsonElement(json)
    } catch(e: MissingFieldException) {
        throw SupabaseEncodingException("Couldn't decode sign up email result. Input: $json")
    }

    override fun encodeCredentials(credentials: Config.() -> Unit): JsonObject = supabaseJson.encodeToJsonElement(Config().apply(credentials)).jsonObject

}