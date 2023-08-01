package io.github.jan.supabase.compose.auth.composable

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.GoogleLoginConfig
import io.github.jan.supabase.compose.auth.getSignInRequest
import io.github.jan.supabase.compose.auth.loginWithGoogle
import io.github.jan.supabase.gotrue.LogoutScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
actual fun ComposeAuth.rememberLoginWithGoogle(
    onResult: (NativeSignInResult) -> Unit,
    fallback: suspend () -> Unit
): NativeSignInState {

    val state = remember { NativeSignInState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val request = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        scope.launch(Dispatchers.IO) {
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                try {
                    val credential =
                        Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                    credential.googleIdToken?.let {
                        loginWithGoogle(it)
                        onResult.invoke(NativeSignInResult.Success())
                    } ?: run {
                        onResult.invoke(NativeSignInResult.Error("error: idToken is missing"))
                    }
                } catch (apiE: ApiException) {
                    when (apiE.statusCode) {
                        CommonStatusCodes.CANCELED -> onResult.invoke(NativeSignInResult.ClosedByUser)
                        CommonStatusCodes.NETWORK_ERROR -> onResult.invoke(NativeSignInResult.NetworkError(apiE.localizedMessage ?: "error"))
                        else -> onResult.invoke(NativeSignInResult.Error(apiE.localizedMessage ?: "error"))
                    }
                } catch (e: Exception) {
                    onResult.invoke(NativeSignInResult.Error(e.localizedMessage ?: "error"))
                }
            }
            state.reset()
        }
    }

    LaunchedEffect(key1 = state.started) {
        // init signInRequest options
        if (state.started) {

            if (config.loginConfig == null || config.loginConfig !is GoogleLoginConfig) {
                fallback.invoke()
                state.reset()
                return@LaunchedEffect
            }

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

@Composable
actual fun ComposeAuth.rememberSignOut(logoutScope: LogoutScope): NativeSignInState {
    val context = LocalContext.current
    return defaultSignOutBehavior(logoutScope) {
        Identity.getSignInClient(context).signOut().await()
    }
}