package io.github.jan.supabase.common.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueConfig
import org.koin.dsl.module

expect fun GoTrueConfig.platformGoTrueConfig()

const val SERVER_CLIENT_ID = "GOOGLE_WEB_CLIENT_ID" //Don't put in your android client id

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "YOUR_URL",
            supabaseKey = "YOUR_KEY"
        ) {
            install(GoTrue) {
                platformGoTrueConfig()
            }
        }
    }
}