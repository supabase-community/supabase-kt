package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.claims.ClaimsResponse
import io.github.jan.supabase.auth.claims.JwtHeader
import io.github.jan.supabase.auth.claims.JwtPayload
import kotlinx.io.bytestring.encode
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64

private const val JWT_PARTS = 3

internal data class JWTData(
    val claimsResponse: ClaimsResponse,
    val rawHeader: String,
    val rawPayload: String
)
private val BASE64_INSTANCE = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
@PublishedApi
internal val JWT_JSON_INSTANCE = Json { isLenient = true }

internal fun decodeJwt(jwt: String): JWTData {
    val parts = jwt.split(".")
    require(parts.size == JWT_PARTS) {
        "Invalid JWT structure"
    }

    val header = JWT_JSON_INSTANCE.decodeFromString<JwtHeader>(parts[0].decodeBase64Url())
    val payload = JwtPayload(JWT_JSON_INSTANCE.decodeFromString<JsonObject>(parts[1].decodeBase64Url()))
    val signature = parts[2].decodeBase64ByteArray()
    return JWTData(
        ClaimsResponse(payload, header, signature),
        parts[0],
        parts[1]
    )
}

internal fun String.encodeToBase64Url() = BASE64_INSTANCE.encode(this.encodeToByteString())

internal fun String.decodeBase64Url() = BASE64_INSTANCE.decode(this).decodeToString()

internal fun String.decodeBase64ByteArray() = BASE64_INSTANCE.decode(this)