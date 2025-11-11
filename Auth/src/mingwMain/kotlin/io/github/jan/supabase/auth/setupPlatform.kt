package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.logging.w

@SupabaseInternal
actual fun Auth.setupPlatform() {
    Auth.logger.w { "Windows support is experimental, please report any bugs you find!" }
    initDone()
}