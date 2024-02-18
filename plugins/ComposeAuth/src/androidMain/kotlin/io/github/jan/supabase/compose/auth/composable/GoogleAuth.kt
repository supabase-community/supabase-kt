package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.applicationContext
import io.github.jan.supabase.compose.auth.getActivity
import io.github.jan.supabase.compose.auth.getGoogleIDOptions
import io.github.jan.supabase.compose.auth.signInWithGoogle

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
    return signInWithCM(onResult, fallback)
}

@Composable
internal fun ComposeAuth.signInWithCM(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState{
    val state = remember { NativeSignInState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = state.started){
        if (state.started) {
            val activity = context.getActivity()

            try {
                if (activity != null && config.googleLoginConfig != null) {
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(getGoogleIDOptions(config.googleLoginConfig))
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
                                            e.localizedMessage ?: "Google id parsing exception",
                                            e
                                        )
                                    )
                                } catch(e: Exception) {
                                    onResult.invoke(
                                        NativeSignInResult.Error(
                                            e.localizedMessage ?: "error",
                                            e
                                        )
                                    )
                                }
                            } else {
                                onResult.invoke(NativeSignInResult.Error("Unexpected type of credential"))
                            }
                        } else -> {
                        onResult.invoke(NativeSignInResult.Error("Unsupported credentials"))
                    }
                    }
                } else {
                    fallback.invoke()
                }
            } catch (e: GetCredentialException) {
                when (e) {
                    is GetCredentialCancellationException -> onResult.invoke(NativeSignInResult.ClosedByUser)
                    else -> onResult.invoke(
                        NativeSignInResult.Error(
                            e.localizedMessage ?: "error: getCredentialException",
                            e
                        )
                    )
                }
            } catch (e: Exception) {
                onResult.invoke(NativeSignInResult.Error(e.localizedMessage ?: "error", e))
            } finally {
                state.reset()
            }
        }
    }

    return state
}

internal actual suspend fun handleGoogleSignOut() {
    CredentialManager.create(applicationContext()).clearCredentialState(ClearCredentialStateRequest())
}