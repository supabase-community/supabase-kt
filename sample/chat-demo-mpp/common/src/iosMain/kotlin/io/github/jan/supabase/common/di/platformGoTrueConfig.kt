package io.github.jan.supabase.common.di

import io.supabase.auth.AuthConfig

actual fun AuthConfig.platformGoTrueConfig() {
    scheme = "io.jan.supabase"
    host = "login"
}