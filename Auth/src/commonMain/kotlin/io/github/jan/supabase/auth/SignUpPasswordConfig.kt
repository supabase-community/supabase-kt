package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.providers.builtin.putCaptchaToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class SignUpPasswordConfig(
    val identifier: LoginIdentifier,
    val password: String
) {

    var data: JsonObject? = null
    var captchaToken: String? = null

    open fun JsonObjectBuilder.putExtraParams() = Unit

    fun encode() = buildJsonObject {
        identifier.put(this)
        captchaToken?.let { putCaptchaToken(it) }
        put("password", password)
        putExtraParams()
    }

}

class EmailSignUpConfig(identifier: LoginIdentifier, password: String): SignUpPasswordConfig(identifier, password) {

    var redirectTo: String? = null

}

class PhoneSignUpConfig(identifier: LoginIdentifier, password: String): SignUpPasswordConfig(identifier, password) {

    var channel: Phone.Channel = Phone.Channel.SMS

    override fun JsonObjectBuilder.putExtraParams() {
        put("channel", channel.value)
    }

}