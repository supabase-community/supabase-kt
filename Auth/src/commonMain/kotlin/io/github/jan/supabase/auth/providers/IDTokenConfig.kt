package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.auth.providers.builtin.CaptchaTokenSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class IdTokenConfig(
    @SerialName("access_token")
    var accessToken: String? = null,
    var nonce: String? = null,
    @Serializable(with = CaptchaTokenSerializer::class)
    @SerialName("gotrue_meta_security")
    var captchaToken: String? = null
)