package io.github.jan.supabase.gotrue

import io.github.aakira.napier.Napier
import io.github.jan.supabase.annotiations.SupabaseInternal

@SupabaseInternal
actual fun GoTrue.setupPlatform() {
    Napier.w { "IOS support is experimental, please report any bugs you find!" }
}