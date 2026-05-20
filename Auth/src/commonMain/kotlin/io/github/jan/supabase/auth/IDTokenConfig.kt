package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.providers.builtin.CaptchaTokenSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IdTokenConfig {

    val provider: IDTokenProvider
    val token: String
    var accessToken: String?
    var nonce: String?
    var captchaToken: String?
    val linkIdentity: Boolean?

}

open class DefaultIdTokenConfig(
    @SerialName("provider")
    override val provider: IDTokenProvider,
    @SerialName("id_token")
    override val token: String,
    @SerialName("access_token")
    override var accessToken: String? = null,
    override var nonce: String? = null,
    @Serializable(with = CaptchaTokenSerializer::class)
    @SerialName("gotrue_meta_security")
    override var captchaToken: String? = null,
    @SerialName("link_identity")
    override val linkIdentity: Boolean? = null
): IdTokenConfig