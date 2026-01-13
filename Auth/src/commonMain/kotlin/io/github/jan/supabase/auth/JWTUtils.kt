package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.claims.ClaimsResponse
import io.github.jan.supabase.auth.claims.JwtHeader
import io.github.jan.supabase.auth.claims.JwtPayload
import io.ktor.util.decodeBase64String
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

private val BASE64_REGEX by lazy {
    Regex("/^([a-z0-9_-]{4})*($|[a-z0-9_-]{3}$|[a-z0-9_-]{2}$)$/i")
}

internal data class JWTData(
    val claimsResponse: ClaimsResponse,
    val rawHeader: String,
    val rawPayload: String
)

internal fun decodeJwt(jwt: String): JWTData {
    val parts = jwt.split(".")

    require(parts.size == 3) {
        "Invalid JWT structure"
    }

    parts.forEach { part ->
        require(BASE64_REGEX.matches(part)) {
            "JWT not in base64url format"
        }
    }

    val header = Json.decodeFromString<JwtHeader>(parts[0].decodeBase64String())
    val payload = Json.decodeFromString<JwtPayload>(parts[1].decodeBase64String())
    val signature = Base64.UrlSafe.decode(jwt)
    return JWTData(
        ClaimsResponse(payload, header, signature),
        parts[0],
        parts[1]
    )
}