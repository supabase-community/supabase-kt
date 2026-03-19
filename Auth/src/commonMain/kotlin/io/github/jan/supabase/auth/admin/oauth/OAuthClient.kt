package io.github.jan.supabase.auth.admin.oauth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the type of an OAuth client.
 */
@Serializable
enum class OAuthClientType {
    @SerialName("public")
    PUBLIC,
    @SerialName("confidential")
    CONFIDENTIAL
}

/**
 * Represents a grant type for an OAuth client.
 */
@Serializable
enum class OAuthClientGrantType {
    @SerialName("authorization_code")
    AUTHORIZATION_CODE,
    @SerialName("refresh_token")
    REFRESH_TOKEN
}

/**
 * Represents a response type for an OAuth client.
 */
@Serializable
enum class OAuthClientResponseType {
    @SerialName("code")
    CODE
}

/**
 * Represents a registration type for an OAuth client.
 */
@Serializable
enum class OAuthClientRegistrationType {
    @SerialName("dynamic")
    DYNAMIC,
    @SerialName("manual")
    MANUAL
}

/**
 * Represents the token endpoint authentication method for an OAuth client.
 */
@Serializable
enum class OAuthClientTokenEndpointAuthMethod {
    @SerialName("none")
    NONE,
    @SerialName("client_secret_basic")
    CLIENT_SECRET_BASIC,
    @SerialName("client_secret_post")
    CLIENT_SECRET_POST
}

/**
 * Represents an OAuth 2.1 client registered with the Supabase Auth server.
 */
@Serializable
data class OAuthClient(
    val id: String,
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String? = null,
    @SerialName("client_name")
    val clientName: String,
    @SerialName("client_type")
    val clientType: OAuthClientType? = null,
    @SerialName("client_uri")
    val clientUri: String? = null,
    @SerialName("logo_uri")
    val logoUri: String? = null,
    @SerialName("redirect_uris")
    val redirectUris: List<String> = emptyList(),
    @SerialName("grant_types")
    val grantTypes: List<OAuthClientGrantType> = emptyList(),
    @SerialName("response_types")
    val responseTypes: List<OAuthClientResponseType> = emptyList(),
    val scope: String? = null,
    @SerialName("token_endpoint_auth_method")
    val tokenEndpointAuthMethod: OAuthClientTokenEndpointAuthMethod? = null,
    @SerialName("registration_type")
    val registrationType: OAuthClientRegistrationType? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
