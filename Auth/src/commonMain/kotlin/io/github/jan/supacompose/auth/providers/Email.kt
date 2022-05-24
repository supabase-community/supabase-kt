package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.supabaseJson
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object Email : DefaultAuthProvider<Email.Config, Email.Result> {

    @Serializable
    data class Config(var email: String = "", var password: String = "")
    @Serializable
    data class Result(
        val id: String,
        val email: String,
        @SerialName("confirmation_sent_at") val confirmationSentAt: Instant,
        @SerialName("created_at") val createdAt: Instant,
        @SerialName("updated_at") val updatedAt: Instant,
    )

    override fun decodeResult(body: String): Result = supabaseJson.decodeFromString(body)

    override fun encodeCredentials(credentials: Config.() -> Unit): String = supabaseJson.encodeToString(Config().apply(credentials))

}