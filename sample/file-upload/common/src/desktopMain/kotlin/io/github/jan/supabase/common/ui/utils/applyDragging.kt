package io.github.jan.supabase.common.ui.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
actual fun Modifier.applyDragging(isDragging: MutableState<Boolean>, onSuccess: (List<String>) -> Unit): Modifier {
    return composed {
        dragAndDropTarget(
            shouldStartDragAndDrop = {
                it.dragData() is DragData.FilesList
            },
            target = object : DragAndDropTarget {
                override fun onDrop(event: DragAndDropEvent): Boolean {
                    isDragging.value = false
                    val dragData = event.dragData() as DragData.FilesList
                    onSuccess(dragData.readFiles())
                    return true;
                }
            }
        )
    }
}