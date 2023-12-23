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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.GoogleLoginConfig
import io.github.jan.supabase.compose.auth.defaultSignOutBehavior
import io.github.jan.supabase.compose.auth.getActivity
import io.github.jan.supabase.compose.auth.getGoogleIDOptions
import io.github.jan.supabase.compose.auth.getSignInRequest
import io.github.jan.supabase.compose.auth.signInWithGoogle
import io.github.jan.supabase.gotrue.SignOutScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Composable function that implements Native Google Auth.
 *
 * On unsupported platforms it will use the [fallback]
 *
 * @param onResult Callback for the result of the login
 * @param fallback Fallback function for unsupported platforms
 * @return [NativeSignInState]
 */
@Composable
actual fun ComposeAuth.rememberSignInWithGoogle(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState {
    return if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.TIRAMISU) {
        signInWithCM(onResult, fallback)
    } else {
        oneTapSignIn(onResult, fallback)
    }
}

@Composable
internal fun ComposeAuth.signInWithCM(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState{
    val state = remember { NativeSignInState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = state.started){
        if (state.started) {
            val activity = context.getActivity()

            if (activity == null || config.loginConfig["google"] == null) {
                fallback.invoke()
                state.reset()
                return@LaunchedEffect
            }

            try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(getGoogleIDOptions(config.loginConfig["google"] as? GoogleLoginConfig))
                    .build()
                val result = CredentialManager.create(context).getCredential(activity, request)

                when (result.credential) {
                    is CustomCredential -> {
                        if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(result.credential.data)
                                signInWithGoogle(googleIdTokenCredential.idToken)
                                onResult.invoke(NativeSignInResult.Success)
                            } catch (e: GoogleIdTokenParsingException) {
                                onResult.invoke(
                                    NativeSignInResult.Error(
                                        e.localizedMessage ?: "error: google id parsing exception"
                                    )
                                )
                            }
                        } else {
                            onResult.invoke(NativeSignInResult.Error("error: unexpected type of credential"))
                        }
                    }
                    else -> {
                        onResult.invoke(NativeSignInResult.Error("error: unsupported credentials"))
                    }
                }
            } catch (e: GetCredentialException) {
                when (e) {
                    is GetCredentialCancellationException -> onResult.invoke(NativeSignInResult.ClosedByUser)
                    else -> onResult.invoke(
                        NativeSignInResult.Error(
                            e.localizedMessage ?: "error: getCredentialException"
                        )
                    )
                }
            } finally {
                state.reset()
            }
        }
    }

    return state
}

@Composable
internal fun ComposeAuth.oneTapSignIn(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState {
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
                        signInWithGoogle(it)
                        onResult.invoke(NativeSignInResult.Success)
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
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                onResult.invoke(NativeSignInResult.Error("error: operation canceled"))
            } else {
                onResult.invoke(NativeSignInResult.Error("error: ${result.resultCode}"))
            }
            state.reset()
        }
    }

    LaunchedEffect(key1 = state.started) {
        if (state.started) {
            if (config.loginConfig["google"] == null) {
                fallback.invoke()
                state.reset()
                return@LaunchedEffect
            }
            val config = config.loginConfig["google"] as GoogleLoginConfig
            val signInRequest = getSignInRequest(config)
            try {
                val oneTapResult = Identity.getSignInClient(context).beginSignIn(signInRequest).await()
                request.launch(
                    IntentSenderRequest.Builder(oneTapResult.pendingIntent.intentSender).build()
                )
            }catch (e:Exception){
                onResult.invoke(NativeSignInResult.Error(e.localizedMessage ?: "error"))
                state.reset()
            }
        }
    }

    return state
}

/**
 * Composable for Google SignOut with native behavior
 */
@OptIn(SupabaseInternal::class)
@Composable
actual fun ComposeAuth.rememberSignOutWithGoogle(signOutScope: SignOutScope): NativeSignInState {
    val context = LocalContext.current
    return defaultSignOutBehavior(signOutScope) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.TIRAMISU) {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } else {
            Identity.getSignInClient(context).signOut().await()
        }
    }
}