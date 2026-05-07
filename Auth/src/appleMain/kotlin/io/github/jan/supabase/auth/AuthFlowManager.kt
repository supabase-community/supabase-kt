package io.github.jan.supabase.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSURL

internal object AuthFlowManager {
    private val _redirectFlow = MutableSharedFlow<NSURL>(replay = 0)
    val redirectFlow = _redirectFlow.asSharedFlow()

    fun handleRedirect(uri: NSURL) {
        CoroutineScope(Dispatchers.Main).launch {
            _redirectFlow.emit(uri)
        }
    }
}