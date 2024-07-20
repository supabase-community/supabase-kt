package io.github.jan.supabase.common

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.provider.OpenableColumns
import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.util.cio.toByteReadChannel
import io.ktor.utils.io.ByteReadChannel

actual val PlatformFile.dataProducer: suspend (offset: Long) -> ByteReadChannel
    get() {
        return {
            val context = applicationContext()
            val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            val fileDescriptor: AssetFileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r") ?: error("Could not open file descriptor")
            val size = fileDescriptor.length
            (context.contentResolver.openInputStream(uri)?.toByteReadChannel() ?: error("Could not open input stream")).apply { discard(it) }
        }
    }