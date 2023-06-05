package io.github.jan.supabase.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

actual open class MPViewModel actual constructor(): ViewModel() {

    actual val coroutineScope = viewModelScope

}