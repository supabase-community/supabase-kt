package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.browser.window

@SupabaseInternal
actual fun Auth.defaultRedirectUrl(): String? = window.location.origin