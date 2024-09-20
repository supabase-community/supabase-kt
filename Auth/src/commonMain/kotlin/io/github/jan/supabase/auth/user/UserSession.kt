@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")
package io.github.jan.supabase.auth.user

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

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
)