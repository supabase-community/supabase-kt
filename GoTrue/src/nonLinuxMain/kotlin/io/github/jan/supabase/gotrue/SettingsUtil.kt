package io.github.jan.supabase.gotrue

import com.russhwolf.settings.Settings
import io.github.jan.supabase.annotations.SupabaseInternal


@SupabaseInternal
fun createDefaultSettings() = try {
    Settings()
} catch(e: Exception) {
    error("Failed to create default settings for SettingsSessionManager. You might have to provide a custom settings instance or a custom session manager. Learn more at https://github.com/supabase-community/supabase-kt/wiki/Session-Saving")
}

@SupabaseInternal
actual fun GoTrue.createDefaultSessionManager(): SessionManager = SettingsSessionManager()

@SupabaseInternal
actual fun GoTrue.createDefaultCodeVerifierCache(): CodeVerifierCache = SettingsCodeVerifierCache()