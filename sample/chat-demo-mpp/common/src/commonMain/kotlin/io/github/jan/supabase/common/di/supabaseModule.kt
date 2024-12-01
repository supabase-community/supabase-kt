package io.github.jan.supabase.common.di

import io.supabase.auth.Auth
import io.supabase.auth.AuthConfig
import io.supabase.auth.FlowType
import io.supabase.createSupabaseClient
import io.supabase.logging.LogLevel
import io.supabase.postgrest.Postgrest
import io.supabase.realtime.Realtime
import org.koin.dsl.module

expect fun AuthConfig.platformGoTrueConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "YOUR_URL",
            supabaseKey = "YOUR_KEY"
        ) {
            defaultLogLevel = LogLevel.DEBUG
            install(Postgrest)
            install(Auth) {
                platformGoTrueConfig()
                flowType = FlowType.PKCE
            }
            install(Realtime)
        }
    }
}