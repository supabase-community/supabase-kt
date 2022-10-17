package io.github.jan.supabase.auth.providers

data class ExternalAuthConfig(
    val params: MutableMap<String, Any> = mutableMapOf()
)