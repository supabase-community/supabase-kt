package io.github.jan.supabase.auth.admin.custom.provider

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Parameters for updating an existing custom provider.
 * All fields are optional. Only provided fields will be updated.
 */
@Serializable
data class CustomProviderUpdateBuilder(
    var name: String? = null,
    @SerialName("client_id")
    var clientId: String? = null,
    @SerialName("client_secret")
    var clientSecret: String? = null,
    @SerialName("acceptable_client_ids")
    var acceptableClientIds: List<String>? = null,
    var scopes: List<String>? = null,
    @SerialName("pkce_enabled")
    var pkceEnabled: Boolean? = null,
    @SerialName("attribute_mapping")
    var attributeMapping: JsonObject? = null,
    @SerialName("authorization_params")
    var authorizationParams: JsonObject? = null,
    var enabled: Boolean? = null,
    @SerialName("email_optional")
    var emailOptional: Boolean? = null,
    var issuer: String? = null,
    @SerialName("discovery_url")
    var discoveryUrl: String? = null,
    @SerialName("skip_nonce_check")
    var skipNonceCheck: Boolean? = null,
    @SerialName("authorization_url")
    var authorizationUrl: String? = null,
    @SerialName("token_url")
    var tokenUrl: String? = null,
    @SerialName("userinfo_url")
    var userinfoUrl: String? = null,
    @SerialName("jwks_uri")
    var jwksUri: String? = null
)