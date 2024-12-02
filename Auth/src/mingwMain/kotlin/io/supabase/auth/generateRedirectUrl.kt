package io.supabase.auth

import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.Auth

@SupabaseInternal
internal actual fun Auth.defaultPlatformRedirectUrl(): String? = null