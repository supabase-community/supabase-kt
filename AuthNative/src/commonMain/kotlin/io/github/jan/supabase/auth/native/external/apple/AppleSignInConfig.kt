package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.DefaultIdTokenConfig
import io.github.jan.supabase.auth.DefaultOAuthConfig
import io.github.jan.supabase.auth.IdTokenConfig
import io.github.jan.supabase.auth.OAuthConfig
import io.github.jan.supabase.auth.OAuthProviders
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = AppleSignInConfigSerializer::class)
class AppleSignInConfig(token: String):
    IdTokenConfig by DefaultIdTokenConfig(OAuthProviders.APPLE, token),
    OAuthConfig by DefaultOAuthConfig()

object AppleSignInConfigSerializer : KSerializer<AppleSignInConfig> {
    override val descriptor: SerialDescriptor = DefaultIdTokenConfig.serializer().descriptor

    override fun serialize(encoder: Encoder, value: AppleSignInConfig) {
        val idTokenDelegate = value.toIdTokenConfig()
        encoder.encodeSerializableValue(DefaultIdTokenConfig.serializer(), idTokenDelegate)
    }

    override fun deserialize(decoder: Decoder): AppleSignInConfig {
        throw UnsupportedOperationException()
    }

    private fun AppleSignInConfig.toIdTokenConfig() = DefaultIdTokenConfig(provider, token, accessToken, nonce, captchaToken, linkIdentity)
}