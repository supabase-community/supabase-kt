package io.github.jan.supabase.auth.admin.custom.provider

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type of custom identity provider.
 */
@Serializable
enum class CustomProviderType {
    @SerialName("oauth2") OAUTH2,
    @SerialName("oidc") OIDC
}