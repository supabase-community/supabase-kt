package io.github.jan.supabase.common

import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.utils.io.ByteReadChannel

actual val PlatformFile.dataProducer: suspend (offset: Long) -> ByteReadChannel
    get() = { offset -> ByteReadChannel(this.readBytes()).also { it.discard(offset) } } //Maybe change this to an actual stream in the future