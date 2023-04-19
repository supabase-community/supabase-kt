package io.github.jan.supabase.storage

import android.annotation.SuppressLint
import android.content.res.AssetFileDescriptor
import android.net.Uri
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

suspend fun ResumableClient.createOrContinueUpload(path: String, uri: Uri, upsert: Boolean = false) = createOrContinueUpload(uri.createByteReader(), uri.toString(), uri.contentSize, path, upsert)

suspend fun ResumableClient.continuePreviousUriUploads() = continuePreviousUploads { source, offset -> Uri.parse(source).createByteReader()(offset) }

@SuppressLint("Recycle")
private suspend fun Uri.createByteReader(): suspend (Long) -> ByteReadChannel = { offset: Long ->
    val context = applicationContext()
    val inputStream = context.contentResolver.openInputStream(this) ?: throw IllegalArgumentException("Uri is not readable")
    inputStream.toByteReadChannel().apply { discard(offset) }
}

private val Uri.contentSize: Long
    @SuppressLint("Recycle")
    get() {
        return (applicationContext().contentResolver.openAssetFileDescriptor(this, "r") ?: error("Could not open file descriptor")).use(AssetFileDescriptor::getLength)
    }