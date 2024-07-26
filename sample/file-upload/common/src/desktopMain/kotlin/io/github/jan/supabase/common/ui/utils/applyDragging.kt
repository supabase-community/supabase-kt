package io.github.jan.supabase.common.ui.utils

import androidx.compose.runtime.MutableState
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.onExternalDrag

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.applyDragging(isDragging: MutableState<Boolean>, onSuccess: (List<String>) -> Unit): Modifier {
    return composed {
        onExternalDrag(
            onDragStart = {
                isDragging.value = true
            },
            onDragExit = {
                isDragging.value = false
            },
            onDrop = {
                isDragging.value = false
                if(it.dragData is DragData.FilesList) {
                    onSuccess((it.dragData as DragData.FilesList).readFiles())
                }
            }
        )
    }
}