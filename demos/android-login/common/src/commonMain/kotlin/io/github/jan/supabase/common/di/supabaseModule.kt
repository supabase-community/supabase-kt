package io.github.jan.supabase.common.di

import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueConfig
import org.koin.dsl.module

expect fun GoTrueConfig.platformGoTrueConfig()

const val SERVER_CLIENT_ID = "1009828726778-ofegbrc0pg8hs9hc8hnb817e52jpjuh2.apps.googleusercontent.com" //Don't put in your android client id

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://uafjlnwkkmubykxywuds.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVhZmpsbndra211YnlreHl3dWRzIiwicm9sZSI6ImFub24iLCJpYXQiOjE2ODg4NTE5MTQsImV4cCI6MjAwNDQyNzkxNH0.ZUGbB-9dHz18Cfwl8CgSpli-xw06JOBTRiY2NnV_Fo0"
        ) {
            install(GoTrue) {
                platformGoTrueConfig()
            }
            install(ComposeAuth) {
                googleNativeLogin(SERVER_CLIENT_ID)
            }
        }
    }
}