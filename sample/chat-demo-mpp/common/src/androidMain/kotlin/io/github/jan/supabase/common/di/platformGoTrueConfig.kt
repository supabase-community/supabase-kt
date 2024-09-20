package io.github.jan.supabase.common.di

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.ExternalAuthAction

actual fun AuthConfig.platformGoTrueConfig() {
    scheme = "io.jan.supabase"
    host = "login"
    defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
}