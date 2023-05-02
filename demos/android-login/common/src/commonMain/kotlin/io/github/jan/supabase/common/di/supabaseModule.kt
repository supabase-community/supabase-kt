package io.github.jan.supabase.common.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueConfig
import org.koin.dsl.module

expect fun GoTrueConfig.platformGoTrueConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "",
            supabaseKey = ""
        ) {
            install(GoTrue) {
                platformGoTrueConfig()
            }
        }
    }
}