package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.providers.builtin.putCaptchaToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class SignInOtpConfig(
    val identifier: LoginIdentifier,
) {

    var data: JsonObject? = null
    var captchaToken: String? = null
    var shouldCreateUser: Boolean = true

    open fun JsonObjectBuilder.putExtraParams() = Unit

    fun encode() = buildJsonObject {
        identifier.put(this)
        captchaToken?.let { putCaptchaToken(it) }
        put("create_user", shouldCreateUser)
        putExtraParams()
    }

}

class EmailSignInOtpConfig(identifier: LoginIdentifier): SignInOtpConfig(identifier) {

    var redirectTo: String? = null

}

class PhoneSignInOtpConfig(identifier: LoginIdentifier): SignInOtpConfig(identifier) {

    var channel: Phone.Channel = Phone.Channel.SMS

    override fun JsonObjectBuilder.putExtraParams() {
        put("channel", channel.value)
    }

}