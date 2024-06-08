package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
internal actual fun Auth.defaultPlatformRedirectUrl(): String? = config.deepLinkOrNull