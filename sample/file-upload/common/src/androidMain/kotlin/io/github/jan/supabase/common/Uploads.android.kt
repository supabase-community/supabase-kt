package io.github.jan.supabase.common

import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.utils.io.ByteReadChannel

actual val PlatformFile.dataProducer: suspend (offset: Long) -> ByteReadChannel
    get() = TODO("Not yet implemented")