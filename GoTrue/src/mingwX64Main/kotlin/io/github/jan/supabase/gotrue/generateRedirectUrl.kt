package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotiations.SupabaseInternal

@SupabaseInternal
actual fun GoTrue.generateRedirectUrl(fallbackUrl: String?): String? = fallbackUrl