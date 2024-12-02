package io.supabase.auth

import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.Auth
import kotlinx.browser.window

@SupabaseInternal
actual fun Auth.defaultPlatformRedirectUrl(): String? = window.location.origin