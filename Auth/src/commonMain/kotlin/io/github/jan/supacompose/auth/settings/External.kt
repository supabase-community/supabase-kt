package io.github.jan.supacompose.auth.settings

@kotlinx.serialization.Serializable
data class External(
    val apple: Boolean,
    val azure: Boolean,
    val bitbucket: Boolean,
    val discord: Boolean,
    val email: Boolean,
    val facebook: Boolean,
    val github: Boolean,
    val gitlab: Boolean,
    val google: Boolean,
    val keycloak: Boolean,
    val linkedin: Boolean,
    val notion: Boolean,
    val phone: Boolean,
    val saml: Boolean,
    val slack: Boolean,
    val spotify: Boolean,
    val twitch: Boolean,
    val twitter: Boolean,
    val workos: Boolean,
    val zoom: Boolean
)