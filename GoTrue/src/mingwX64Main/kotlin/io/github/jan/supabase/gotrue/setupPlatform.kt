package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun GoTrue.setupPlatform() {
    Logger.w { "Windows support is experimental, please report any bugs you find!" }
}