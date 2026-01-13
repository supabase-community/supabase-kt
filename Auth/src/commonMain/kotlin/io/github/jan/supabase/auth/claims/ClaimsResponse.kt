package io.github.jan.supabase.auth.claims

import io.github.jan.supabase.auth.AMREntry
import io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

data class ClaimsResponse(
    val claims: JwtPayload,
    val header: JwtHeader,
    val signature: ByteArray
)

data class JwtHeader(
    val alg: Algorithm,
    val kid: String,
    val typ: String
) {
    enum class Algorithm {
        RS256, ES256, HS256
    }
}

data class JwtPayload(
    // required claims
    val iss: String,
    val sub: String,
    val aud: List<String>,
    val exp: Instant,
    val iat: Instant,
    val role: String,
    val aal: AuthenticatorAssuranceLevel,
    @SerialName("session_id")
    val sessionId: String,

    // standard optional claims
    val email: String? = null,
    val phone: String? = null,
    @SerialName("is_anonymous")
    val isAnonymous: Boolean? = null,

    // optional claims
    val jti: String? = null,
    val nbf: Instant? = null,
    @SerialName("app_metadata")
    val appMetadata: JsonObject? = null,
    @SerialName("user_metadata")
    val userMetadata: JsonObject? = null,
    val amr: List<AMREntry>? = null,

    // special claims
    val ref: String? = null,

    private val customClaims: Map<String, JsonElement>
)