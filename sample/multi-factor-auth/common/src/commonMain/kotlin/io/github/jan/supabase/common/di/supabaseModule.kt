package io.github.jan.supabase.common.di

import io.supabase.auth.Auth
import io.supabase.auth.AuthConfig
import io.supabase.createSupabaseClient
import org.koin.dsl.module

expect fun AuthConfig.platformGoTrueConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "YOUR_URL",
            supabaseKey = "YOUR_KEY"
        ) {
            install(Auth) {
                platformGoTrueConfig()
            }
        }
    }
}