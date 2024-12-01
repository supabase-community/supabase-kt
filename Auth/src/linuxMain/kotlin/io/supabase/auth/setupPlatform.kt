package io.supabase.auth

import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.Auth
import io.supabase.logging.w

@SupabaseInternal
actual fun Auth.setupPlatform() {
    Auth.logger.w { "Linux support is experimental, please report any bugs you find!" }
}