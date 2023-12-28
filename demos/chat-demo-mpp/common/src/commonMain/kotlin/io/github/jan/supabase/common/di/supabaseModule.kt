package io.github.jan.supabase.common.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.AuthConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import org.koin.dsl.module

expect fun AuthConfig.platformGoTrueConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFybnlmYWV1c2t5cWZ4a3ZvdGdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTMwMzkxMTEsImV4cCI6MTk2ODYxNTExMX0.ItmL8lfnOL9oy7CEX9N6TnYt10VVhk-KTlwley4aq1M"
        ) {
            install(Postgrest)
            install(Auth) {
                platformGoTrueConfig()
            }
            install(Realtime)
        }
    }
    single {
        get<SupabaseClient>().channel("messages")
    }
}