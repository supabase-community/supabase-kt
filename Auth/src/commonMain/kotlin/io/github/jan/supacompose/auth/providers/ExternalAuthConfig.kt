package io.github.jan.supacompose.auth.providers

data class ExternalAuthConfig(
    val params: MutableMap<String, Any> = mutableMapOf()
)