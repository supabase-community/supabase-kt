package io.github.jan.supabase.common.ui.utils

import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

expect fun Modifier.applyDragging(isDragging: MutableState<Boolean>, onSuccess: (List<String>) -> Unit): Modifier