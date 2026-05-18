package io.github.jan.supabase.auth.native.deeplinks

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal object AuthFlowManager {
    private val _redirectFlow = MutableSharedFlow<Uri>(replay = 0)
    val redirectFlow = _redirectFlow.asSharedFlow()

    fun handleRedirect(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            _redirectFlow.emit(uri)
        }
    }
}