package io.github.jan.supabase.auth.native

import android.net.Uri
import io.github.jan.supabase.auth.native.external.google.GoogleCredentialResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

internal object AuthFlowManager {
    private val _deeplinkChannel = Channel<Uri>(Channel.BUFFERED)
    val deeplinks = _deeplinkChannel.receiveAsFlow()

    private var pendingSignIn: CompletableDeferred<GoogleCredentialResult>? = null

    fun handleRedirect(uri: Uri) {
        _deeplinkChannel.trySend(uri)
    }

    fun prepareSignInWait(): CompletableDeferred<GoogleCredentialResult> {
        synchronized(this) {
            if (pendingSignIn != null) {
                error("A sign-in request is already in progress.")
            }

            val deferred = CompletableDeferred<GoogleCredentialResult>()
            pendingSignIn = deferred
            return deferred
        }
    }

    fun handleSignInResult(result: GoogleCredentialResult) {
        synchronized(this) {
            pendingSignIn?.complete(result)
            pendingSignIn = null
        }
    }
}