package io.github.jan.supabase.common.di

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.native.nativeConfig

actual fun AuthConfig.platformGoTrueConfig() {
    nativeConfig {
        nativeAuth {
            googleClientId = "178705897393-1o04rilnoit4a6ls84d2751a3jvibbij.apps.googleusercontent.com"
        }
    }
}