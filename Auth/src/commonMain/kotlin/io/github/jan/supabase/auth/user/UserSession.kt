@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")
package io.github.jan.supabase.auth.user

import io.github.jan.supabase.StringMasking
import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Serializable
data class UserSession(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("provider_refresh_token")
    val providerRefreshToken: String? = null,
    @SerialName("provider_token")
    val providerToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("token_type")
    val tokenType: String,
    val user: UserInfo? = null,
    @SerialName("type")
    val type: String = "",
    val expiresAt: Instant = Clock.System.now() + (expiresIn.seconds),
) {

    /**
     * Renders this session with all bearer credentials masked.
     *
     * The data class default would print [accessToken], [refreshToken], [providerToken] and
     * [providerRefreshToken] verbatim, which puts long-lived credentials into any log line,
     * exception message or crash report that interpolates a session — directly, or transitively
     * via a type that holds one (e.g. `SessionSource.Refresh`).
     *
     * Masking here rather than at each call site means every present and future interpolation is
     * safe by default. Use [unsafeToString] when you deliberately need the raw values.
     */
    @OptIn(SupabaseInternal::class)
    override fun toString(): String = "UserSession(" +
            "accessToken=${StringMasking.maskString(accessToken, showLength = true)}, " +
            "refreshToken=${StringMasking.maskString(refreshToken, showLength = true)}, " +
            "providerRefreshToken=${providerRefreshToken?.let { StringMasking.maskString(it, showLength = true) }}, " +
            "providerToken=${providerToken?.let { StringMasking.maskString(it, showLength = true) }}, " +
            "expiresIn=$expiresIn, " +
            "tokenType=$tokenType, " +
            "user=$user, " +
            "type=$type, " +
            "expiresAt=$expiresAt" +
            ")"

}

/**
 * Renders this session with the raw, unmasked token values.
 *
 * Intended for local debugging only. Never write the result to a log, an exception message or any
 * sink that may be collected by a crash/analytics reporter.
 */
@SupabaseInternal
fun UserSession.unsafeToString(): String = "UserSession(" +
        "accessToken=$accessToken, " +
        "refreshToken=$refreshToken, " +
        "providerRefreshToken=$providerRefreshToken, " +
        "providerToken=$providerToken, " +
        "expiresIn=$expiresIn, " +
        "tokenType=$tokenType, " +
        "user=$user, " +
        "type=$type, " +
        "expiresAt=$expiresAt" +
        ")"
