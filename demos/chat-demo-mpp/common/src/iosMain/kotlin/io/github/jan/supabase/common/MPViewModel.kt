package io.github.jan.supabase.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual open class MPViewModel actual constructor() {
    actual val coroutineScope = CoroutineScope(Dispatchers.Default)

}