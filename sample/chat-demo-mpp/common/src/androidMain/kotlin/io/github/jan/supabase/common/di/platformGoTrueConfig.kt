package io.github.jan.supabase.common.di

import io.supabase.auth.AuthConfig
import io.supabase.auth.ExternalAuthAction

actual fun AuthConfig.platformGoTrueConfig() {
    scheme = "io.jan.supabase"
    host = "login"
    defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
}