package io.github.jan.supabase.common

import io.ktor.utils.io.ByteReadChannel

actual class MPFile(
    actual val name: String,
    actual val source: String,
    actual val size: Long,
    actual val extension: String = name.substringAfter("."),
    actual val dataProducer: suspend (Long) -> ByteReadChannel,
)