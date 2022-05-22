package io.github.jan.supacompose.auth.providers

import com.soywiz.klock.DateTimeTz
import io.github.jan.supacompose.auth.serializers.DateTimeSerializer
import io.github.jan.supacompose.supabaseJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object Phone : DefaultAuthProvider<Phone.Config, Phone.Result> {

    @Serializable
    data class Config(@SerialName("phone") var phoneNumber: String = "", var password: String = "")
    @Serializable
    data class Result(
        val id: String,
        val email: String,
        @Serializable(with = DateTimeSerializer::class) @SerialName("confirmation_sent_at") val confirmationSentAt: DateTimeTz,
        @Serializable(with = DateTimeSerializer::class) @SerialName("created_at") val createdAt: DateTimeTz,
        @Serializable(with = DateTimeSerializer::class) @SerialName("updated_at") val updatedAt: DateTimeTz,
    )

    override fun decodeResult(body: String): Result = supabaseJson.decodeFromString(body)

    override fun encodeCredentials(credentials: Config.() -> Unit): String = supabaseJson.encodeToString(Config().apply(credentials))

}