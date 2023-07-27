package io.github.temk0.supabase.authui.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import io.github.temk0.supabase.authui.AuthUI
import io.github.temk0.supabase.authui.AuthUIImpl
import io.github.temk0.supabase.authui.GoogleLoginConfig
import io.github.temk0.supabase.authui.getSignInRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
actual fun AuthUI.rememberLoginWithGoogle(
    onResult: (NativeSignInResult) -> Unit,
    fallback: () -> Unit
): NativeSignInState {
    this as AuthUIImpl

    val state = remember { NativeSignInState() }
    val context = LocalContext.current

    // init signInRequest options
    if (config.loginConfig == null || config.loginConfig !is GoogleLoginConfig) {
        fallback.invoke()
        state.reset()
        return state
    }

    val request = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        CoroutineScope(Dispatchers.IO).launch {
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                try {
                    val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                    loginWithGoogle(credential.googleIdToken ?: "")
                    onResult.invoke(NativeSignInResult.Success)
                } catch (apiE: ApiException) {
                    when (apiE.statusCode) {
                        CommonStatusCodes.CANCELED -> onResult.invoke(NativeSignInResult.ClosedByUser)
                        CommonStatusCodes.NETWORK_ERROR -> onResult.invoke(NativeSignInResult.NetworkError(apiE.localizedMessage ?: "error"))
                        else -> onResult.invoke(NativeSignInResult.Error(apiE.localizedMessage ?: "error"))
                    }
                } catch (e: Exception) { onResult.invoke(NativeSignInResult.Error(e.localizedMessage ?: "error")) }
            }
            state.reset()
        }
    }

    LaunchedEffect(key1 = state.started) {
        if (state.started) {
            val config = config.loginConfig as GoogleLoginConfig?
            val signInRequest = getSignInRequest(config)
            val oneTapResult = Identity.getSignInClient(context).beginSignIn(signInRequest).await()
            request.launch(
                IntentSenderRequest.Builder(oneTapResult.pendingIntent.intentSender).build()
            )
        }
    }

    return state
}