package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.providers.builtin.putCaptchaToken
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class SignInPasswordConfig(
    val identifier: LoginIdentifier,
    val password: String
) {

    var captchaToken: String? = null

    fun encode() = buildJsonObject {
        identifier.put(this)
        captchaToken?.let { putCaptchaToken(it) }
        put("password", password)
    }

}