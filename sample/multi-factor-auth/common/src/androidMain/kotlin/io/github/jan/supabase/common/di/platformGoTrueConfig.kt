package io.github.jan.supabase.common.di

import io.github.jan.supabase.gotrue.AuthConfig

actual fun AuthConfig.platformGoTrueConfig() {
    scheme = "io.jan.supabase"
    host = "login"
}