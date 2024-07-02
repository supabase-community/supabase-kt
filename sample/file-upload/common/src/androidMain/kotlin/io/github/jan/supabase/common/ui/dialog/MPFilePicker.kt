package io.github.jan.supabase.common.ui.dialog

import android.annotation.SuppressLint
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import io.github.jan.supabase.common.MPFile
import io.ktor.utils.io.jvm.javaio.toByteReadChannel


@SuppressLint("Recycle")
@Composable
actual fun MPFilePicker(
    showFileDialog: Boolean,
    onFileSelected: (MPFile) -> Unit,
    close: () -> Unit
) {
    val context = LocalContext.current
    FilePicker(showFileDialog, fileExtension = "jpg") {
        it?.let { uriString ->
            val uri = Uri.parse(uriString)
            val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            val fileDescriptor: AssetFileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r") ?: error("Could not open file descriptor")
            val size = fileDescriptor.length
            onFileSelected(
                MPFile(
                    name = name,
                    source = uriString,
                    size = size,
                    dataProducer = {
                        (context.contentResolver.openInputStream(uri)?.toByteReadChannel() ?: error("Could not open input stream")).apply { discard(it) }
                    }
                )
            )
        }
        close()
    }
}