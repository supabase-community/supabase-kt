package io.github.jan.supabase.common.ui.dialog

import androidx.compose.runtime.Composable
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import io.github.jan.supabase.common.MPFile
import java.nio.file.Paths

@Composable
actual fun MPFilePicker(
    showFileDialog: Boolean,
    onFileSelected: (MPFile) -> Unit,
    close: () -> Unit
) {
    FilePicker(showFileDialog, fileExtension = "jpg") {
        it?.let { path ->
            onFileSelected(MPFile(Paths.get(path)))
        }
        close()
    }
}