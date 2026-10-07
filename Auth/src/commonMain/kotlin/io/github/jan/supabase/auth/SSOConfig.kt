package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.providers.builtin.putCaptchaToken
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface SSOIdentifier {

    fun put(builder: JsonObjectBuilder)

}

data class SSODomain(val domain: String): SSOIdentifier {

    override fun put(builder: JsonObjectBuilder) {
        builder.put("domain", domain)
    }

}

data class SSOProvider(val providerId: String): SSOIdentifier {

    override fun put(builder: JsonObjectBuilder) {
        builder.put("provider_id", providerId)
    }

}

data class SSOConfig(
    val identifier: SSOIdentifier
) {

    var redirectTo: String? = null
    var captchaToken: String? = null

    fun encode() = buildJsonObject {
        identifier.put(this)
        captchaToken?.let { putCaptchaToken(it) }
        redirectTo?.let { put("redirect_to", it) }
    }

}
