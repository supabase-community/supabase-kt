package io.github.jan.supabase.common

import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.util.cio.readChannel
import io.ktor.utils.io.ByteReadChannel

actual val PlatformFile.dataProducer: suspend (offset: Long) -> ByteReadChannel get() = { offset ->
    this.file.readChannel(start = offset)
}