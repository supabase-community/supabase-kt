package io.github.jan.supabase.auth

import io.github.jan.supabase.plugins.CustomSerializationConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig: CustomSerializationConfig, WebAuthConfig()