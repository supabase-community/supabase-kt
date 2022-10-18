package io.github.jan.supabase.gotrue.providers

data class ExternalAuthConfig(
    val params: MutableMap<String, Any> = mutableMapOf()
)