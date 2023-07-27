package io.github.temk0.supabase.authui.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import io.github.temk0.supabase.authui.AuthUI
import io.github.temk0.supabase.authui.AuthUIImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
actual fun AuthUI.loginWithGoogle(): () -> Unit {
    this as AuthUIImpl

    val scope = CoroutineScope(Dispatchers.IO)

    val tokenIdRequestOptions = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
    config.idTokenRequest?.let { options ->
        tokenIdRequestOptions
            .setServerClientId(options.serverCliendId)
            .setSupported(options.isSupported)
            .setFilterByAuthorizedAccounts(options.filterByAuthorizedAccounts)
            .setNonce(options.nonce)
    }
    config.idTokenRequest?.associateLinkedAccounts?.let {
        tokenIdRequestOptions.associateLinkedAccounts(it.first, it.second)
    }

    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(tokenIdRequestOptions.build())
        .build()

    val oneTabClient = Identity.getSignInClient(LocalContext.current)


    val request = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            scope.launch {
                val credential = oneTabClient.getSignInCredentialFromIntent(result.data)
                loginWithGoogle(credential.googleIdToken ?: "")
            }
        }
    }

    return {
        scope.launch {
            val oneTapResult = oneTabClient.beginSignIn(signInRequest).await()
            request.launch(
                IntentSenderRequest.Builder(oneTapResult.pendingIntent.intentSender).build()
            )
        }
    }
}