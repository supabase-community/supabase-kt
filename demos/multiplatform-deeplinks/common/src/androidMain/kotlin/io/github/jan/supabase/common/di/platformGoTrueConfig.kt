package io.github.jan.supabase.common.di

import io.github.jan.supabase.gotrue.GoTrueConfig

actual fun GoTrueConfig.platformGoTrueConfig() {
    scheme = "io.github.jan.supabase"
    host = "login"
}