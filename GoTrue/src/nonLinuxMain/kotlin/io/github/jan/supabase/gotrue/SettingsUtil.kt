package io.github.jan.supabase.gotrue

import com.russhwolf.settings.Settings
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.util.PlatformUtils.IS_NODE


@SupabaseInternal
fun createDefaultSettings() = try {
    Settings()
} catch(e: Exception) {
    error("Failed to create default settings for SettingsSessionManager. You might have to provide a custom settings instance or a custom session manager. Learn more at https://github.com/supabase-community/supabase-kt/wiki/Session-Saving")
}

@SupabaseInternal
actual fun GoTrue.createDefaultSessionManager(): SessionManager = if(!IS_NODE) SettingsSessionManager() else MemorySessionManager()
@SupabaseInternal
actual fun GoTrue.createDefaultCodeVerifierCache(): CodeVerifierCache = if(!IS_NODE) SettingsCodeVerifierCache() else MemoryCodeVerifierCache()