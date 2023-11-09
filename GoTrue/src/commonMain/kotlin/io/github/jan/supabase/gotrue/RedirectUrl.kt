package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
expect fun Auth.generateRedirectUrl(fallbackUrl: String?): String?