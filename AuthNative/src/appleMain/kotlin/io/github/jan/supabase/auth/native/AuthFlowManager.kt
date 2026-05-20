package io.github.jan.supabase.auth.native

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import platform.Foundation.NSURL

internal object AuthFlowManager {
    private val _deeplinkChannel = Channel<NSURL>(Channel.BUFFERED)
    val deeplinks = _deeplinkChannel.receiveAsFlow()

    fun handleRedirect(uri: NSURL) {
        _deeplinkChannel.trySend(uri)
    }
}