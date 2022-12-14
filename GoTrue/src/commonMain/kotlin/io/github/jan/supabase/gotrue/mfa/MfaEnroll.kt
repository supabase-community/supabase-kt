package io.github.jan.supabase.gotrue.mfa

data class MfaEnroll<T>(
    val id: String,
    val type: String,
    val data: T
)
