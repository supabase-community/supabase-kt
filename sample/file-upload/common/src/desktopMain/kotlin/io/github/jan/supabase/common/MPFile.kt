package io.github.jan.supabase.common

import io.ktor.util.cio.readChannel
import io.ktor.utils.io.ByteReadChannel
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.name

actual class MPFile(private val path: Path) {

    actual val name: String = path.name

    actual val source: String = path.absolutePathString()

    actual val extension: String = path.extension

    actual val size: Long = path.fileSize()

    actual val dataProducer: suspend (Long) -> ByteReadChannel = {
        path.readChannel().apply { discard(it) }
    }

}