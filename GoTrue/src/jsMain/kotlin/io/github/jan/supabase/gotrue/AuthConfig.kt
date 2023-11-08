package io.github.jan.supabase.gotrue

import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.MainConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig: MainConfig, CustomSerializationConfig, AuthConfigDefaults()