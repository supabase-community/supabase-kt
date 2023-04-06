package io.github.jan.supabase.gotrue

import io.github.aakira.napier.Napier

actual fun GoTrue.setupPlatform() {
    Napier.w { "IOS support is highly experimental, please report any bugs you find!" }
}