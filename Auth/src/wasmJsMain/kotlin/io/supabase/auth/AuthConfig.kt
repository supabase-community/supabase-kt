package io.supabase.auth

import io.supabase.plugins.CustomSerializationConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig: CustomSerializationConfig, AuthConfigDefaults()