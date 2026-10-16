package io.github.jan.supabase.auth.native.external.google

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

@Serializable(with = GoogleSignInConfigSerializer::class)
class GoogleSignInConfig(token: String):
    IdTokenConfig by DefaultIdTokenConfig(OAuthProviders.GOOGLE, token),
    OAuthConfig by DefaultOAuthConfig()
{

    var type: GoogleDialogType = GoogleDialogType.DIALOG

}

object GoogleSignInConfigSerializer : KSerializer<GoogleSignInConfig> {
    override val descriptor: SerialDescriptor = DefaultIdTokenConfig.serializer().descriptor

    override fun serialize(encoder: Encoder, value: GoogleSignInConfig) {
        val idTokenDelegate = value.toIdTokenConfig()
        encoder.encodeSerializableValue(DefaultIdTokenConfig.serializer(), idTokenDelegate)
    }

    override fun deserialize(decoder: Decoder): GoogleSignInConfig {
        throw UnsupportedOperationException()
    }

    private fun GoogleSignInConfig.toIdTokenConfig() = DefaultIdTokenConfig(provider, token, accessToken, nonce, captchaToken, linkIdentity)
}