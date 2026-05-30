package io.github.jan.supabase.common.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.native.withNative
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.dsl.module

expect fun AuthConfig.platformGoTrueConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co",
            supabaseKey = "sb_publishable_5YwzP-KWBn56-aIVeg1Gqg_E37hSpmh"
        ) {
            defaultLogLevel = LogLevel.DEBUG
            install(Postgrest)
            install(Auth.withNative()) {
                platformGoTrueConfig()
                flowType = FlowType.PKCE
            }
            install(Realtime)
        }
    }
}