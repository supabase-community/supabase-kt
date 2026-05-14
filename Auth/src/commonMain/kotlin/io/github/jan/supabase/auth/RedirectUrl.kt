package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
internal expect fun Auth.defaultPlatformRedirectUrl(): String?