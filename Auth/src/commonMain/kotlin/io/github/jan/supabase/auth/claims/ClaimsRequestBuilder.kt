package io.github.jan.supabase.auth.claims

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ClaimsRequestBuilder(
    var allowExpired: Boolean = false,
    val jwks: MutableList<JWK> = mutableListOf()
)

@Serializable
data class JWK(
    val kty: KeyType,
    @SerialName("key_ops")
    val keyOps: List<String>,
    val alg: String? = null,
    val kid: String? = null,
    val extraParams: Map<String, String> = emptyMap()
) {

    @Serializable
    enum class KeyType(val value: String) {
        RSA("RSA"), EC("EC"), @SerialName("oct") OCT("oct");
    }

}