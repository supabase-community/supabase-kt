package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.browser.window

@SupabaseInternal
internal actual fun Auth.defaultPlatformRedirectUrl(): String? = window.location.origin