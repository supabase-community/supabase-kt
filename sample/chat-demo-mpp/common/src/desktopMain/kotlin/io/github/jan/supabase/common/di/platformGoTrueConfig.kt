package io.github.jan.supabase.common.di

import io.supabase.auth.AuthConfig

actual fun AuthConfig.platformGoTrueConfig() {
    httpCallbackConfig {
        this.htmlTitle = "Chat App"
    }
}