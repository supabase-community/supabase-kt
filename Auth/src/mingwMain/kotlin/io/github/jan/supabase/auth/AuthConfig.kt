package io.github.jan.supabase.auth

import io.github.jan.supabase.plugins.CustomSerializationConfig

actual class AuthConfig : CustomSerializationConfig, AuthConfigDefaults()