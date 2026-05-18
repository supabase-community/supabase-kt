package io.github.jan.supabase.auth.native.native

import io.github.jan.supabase.auth.Auth

internal expect suspend fun Auth.setupNativePlatform()