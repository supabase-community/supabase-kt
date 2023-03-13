package io.github.jan.supabase.gotrue.providers

sealed interface IDTokenProvider {
    val name: String
}