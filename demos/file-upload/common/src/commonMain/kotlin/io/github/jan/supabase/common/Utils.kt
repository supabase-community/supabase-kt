package io.github.jan.supabase.common

import androidx.compose.ui.ExperimentalComposeUiApi
import io.ktor.utils.io.ByteReadChannel

@OptIn(ExperimentalComposeUiApi::class)
expect fun parseFileTreeFromURIs(paths: List<String>): List<MPFile>

expect fun parseFileTreeFromPath(path: String): List<MPFile>

suspend fun ByteReadChannel.readAllBytes(size: Long): ByteArray {
    val buffer = ByteArray(size.toInt())
    var read = 0
    while(read < size) {
        read += readAvailable(buffer, read, size.toInt() - read)
    }
    return buffer
}