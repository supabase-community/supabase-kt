package io.github.jan.supabase.common.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.AuthConfig
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
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