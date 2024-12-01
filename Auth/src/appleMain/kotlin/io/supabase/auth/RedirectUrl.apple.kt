package io.supabase.auth

import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.Auth
import io.supabase.auth.deepLinkOrNull

@SupabaseInternal
internal actual fun Auth.defaultPlatformRedirectUrl(): String? = config.deepLinkOrNull