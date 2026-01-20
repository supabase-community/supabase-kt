@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")
package io.github.jan.supabase.auth.claims

import io.github.jan.supabase.auth.AMREntry
import io.github.jan.supabase.auth.decodeValue
import io.github.jan.supabase.auth.mfa.AuthenticatorAssuranceLevel
import io.github.jan.supabase.auth.optional
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Instant

/**
 * JWT Payload containing claims for Supabase authentication tokens.
 *
 * Required claims (iss, aud, exp, iat, sub, role, aal, session_id) are inherited from RequiredClaims.
 * All other claims are optional as they can be customized via Custom Access Token Hooks.
 *
 * see https://supabase.com/docs/guides/auth/jwt-fields
 */
class ClaimsResponse(
    val claims: JwtPayload,
    val header: JwtHeader,
    val signature: ByteArray
)

data class JwtHeader(
    val alg: Algorithm,
    val kid: String? = null,
    val typ: String? = null
) {
    enum class Algorithm {
        RS256, ES256, HS256
    }
}

class JwtPayload(
    val claims: JsonObject,
) {

    // required claims
    val iss: String? by claims.optional
    val sub: String? by claims.optional
    val aud: List<String>? = when(val value = claims["aud"]) {
        is JsonArray -> Json.decodeFromJsonElement(value)
        is JsonPrimitive -> listOf(Json.decodeFromJsonElement(value))
        else -> null
    }
    val exp: Instant? by claims.optional
    val iat: Instant? by claims.optional
    val role: String? by claims.optional
    val aal: AuthenticatorAssuranceLevel? by claims.optional
    val sessionId: String? by claims.optional.withKey("session_id")

    // standard optional claims
    val email: String? by claims.optional
    val phone: String? by claims.optional
    val isAnonymous: Boolean? by claims.optional.withKey("is_anonymous")

    // optional claims
    val jti: String? by claims.optional
    val nbf: Instant? by claims.optional
    val appMetadata: JsonObject? by claims.optional.withKey("app_metadata")
    val userMetadata: JsonObject? by claims.optional.withKey("user_metadata")
    val amr: List<AMREntry>? by claims.optional

    // special claims
    val ref: String? by claims.optional

    inline fun <reified T> getClaimOrNull(key: String): T? = claims.decodeValue(key)

    inline fun <reified T> getClaim(key: String) = getClaimOrNull<T>(key) ?: error("Param not found")

}