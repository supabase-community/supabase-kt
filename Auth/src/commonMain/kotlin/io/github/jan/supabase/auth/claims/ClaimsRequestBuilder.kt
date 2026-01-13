package io.github.jan.supabase.auth.claims

data class ClaimsRequestBuilder(
    var allowExpired: Boolean? = null,
    val jwks: MutableList<JWK>
)

data class JWK(
    val kty: KeyType,
    val keyOps: List<String>,
    val alg: String? = null,
    val kid: String? = null,
    val extraParams: Map<String, String>
) {

    enum class KeyType(val value: String) {
        RSA("RSA"), EC("EC"), OCT("oct")
    }

}