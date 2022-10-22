package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.supabaseJson
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

/**
 * Authentication method with phone numbers and password
 */
object Phone : DefaultAuthProvider<Phone.Config, Phone.Result> {

    data class Config(var phoneNumber: String = ""): DefaultAuthProvider.Config()
    @Serializable
    data class Result(
        val id: String,
        val phone: String,
        @SerialName("confirmation_sent_at") val confirmationSentAt: Instant,
        @SerialName("created_at") val createdAt: Instant,
        @SerialName("updated_at") val updatedAt: Instant,
    )

    override fun decodeResult(json: JsonObject): Result = supabaseJson.decodeFromJsonElement(json)

    override fun encodeCredentials(credentials: Config.() -> Unit): String = supabaseJson.encodeToString(Config().apply(credentials))

}