package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.Auth

internal expect fun Auth.defaultPlatformRedirectUrl(): String?