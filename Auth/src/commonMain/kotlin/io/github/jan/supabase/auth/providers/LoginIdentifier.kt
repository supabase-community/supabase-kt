package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.providers.builtin.CaptchaTokenSerializer
import io.github.jan.supabase.auth.providers.builtin.Phone.Channel
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

sealed class LoginIdentifier<C> {

    abstract fun encodeConfig(config: C): JsonObject

    @Serializable
    open class Config(
        var data: JsonObject? = null,
        var redirectUrl: String? = null,
        @Serializable(with = CaptchaTokenSerializer::class)
        @SerialName("gotrue_meta_security")
        var captchaToken: String? = null)

}

data class Email(val address: String) : LoginIdentifier<Email.Config>() {

    @Serializable
    class Config: LoginIdentifier.Config()

    override fun encodeConfig(config: Config): JsonObject {
        return supabaseJson.encodeToJsonElement(config).jsonObject
    }

}
data class Phone(val number: String) : LoginIdentifier<Phone.Config>() {

    @Serializable
    data class Config(var channel: Channel = Channel.SMS): LoginIdentifier.Config()

    override fun encodeConfig(config: Config): JsonObject {
        return supabaseJson.encodeToJsonElement(config).jsonObject
    }

}