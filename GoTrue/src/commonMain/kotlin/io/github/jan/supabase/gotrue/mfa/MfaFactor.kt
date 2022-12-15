package io.github.jan.supabase.gotrue.mfa

data class MfaFactor<T>(
    val id: String,
    val type: String,
    val data: T
)
