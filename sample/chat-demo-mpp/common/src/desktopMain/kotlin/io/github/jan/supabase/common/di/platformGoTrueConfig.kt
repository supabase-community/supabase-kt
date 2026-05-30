package io.github.jan.supabase.common.di

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.native.nativeConfig

actual fun AuthConfig.platformGoTrueConfig() {
    nativeConfig {
        httpCallbackConfig {
            this.htmlTitle = "Chat App"
        }
    }
}