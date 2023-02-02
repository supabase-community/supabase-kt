package io.github.jan.supabase.common.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.realtime
import org.koin.dsl.module

expect fun GoTrueConfig.platformGoTrueConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "YOUR_URL",
            supabaseKey = "YOUR_KEY"
        ) {
            install(Postgrest)
            install(GoTrue) {
                platformGoTrueConfig()
            }
            install(Realtime)
        }
    }
    single {
        get<SupabaseClient>().realtime.createChannel("messages")
    }
}