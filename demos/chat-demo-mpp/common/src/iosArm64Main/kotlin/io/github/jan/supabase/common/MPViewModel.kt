package io.github.jan.supabase.common

import kotlinx.coroutines.CoroutineScope

actual open class MPViewModel actual constructor() {
    actual val coroutineScope: CoroutineScope
        get() = TODO("Not yet implemented")

}