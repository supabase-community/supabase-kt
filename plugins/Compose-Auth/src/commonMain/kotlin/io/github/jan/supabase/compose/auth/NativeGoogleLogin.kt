package io.github.jan.supabase.compose.auth

import androidx.compose.runtime.Composable
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.GoTrue

/**
 * Composable that starts the native Google sign in flow.
 *
 * @sample NativeGoogleLoginExample
 * @param state The state of the sign-in flow. Call [NativeSignInState.startFlow] to start the flow in e.g. your Button.
 * @param clientId The client id of your Google app. **Note:** This should be the same as the one you use for the web sign in, an Android client id will not work but is required for Google OneTap.
 * @param nonce The optional nonce to use for the sign in.
 * @param onResult The callback that is called when the sign-in flow is finished or failed.
 * @param fallback The optional fallback that is called when there is no native sign-in available. That could for example call [GoTrue.loginWith], so the OAuth flow is used instead.
 */
@Composable
@SupabaseExperimental
expect fun NativeGoogleLogin(
    state: NativeSignInState,
    clientId: String,
    nonce: String? = null,
    onResult: (NativeSignInResult) -> Unit,
    fallback: (() -> Unit)? = null
)

@Composable
private fun NativeGoogleLoginExample() {
    val state = rememberNativeSignInState()
    NativeGoogleLogin(
        state = state,
        clientId = "YOUR_CLIENT_ID",
        onResult = { result: NativeSignInResult ->
            when(result) {
                NativeSignInResult.AccountNotFound -> {} //show error dialog
                NativeSignInResult.Canceled -> {} //show error dialog
                NativeSignInResult.ClosedByUser -> {} //show error dialog
                NativeSignInResult.NetworkError -> {} //show error dialog
                is NativeSignInResult.Error -> { println(result.message) }
                is NativeSignInResult.Success -> {
                    /*call gotrue.loginWith(IDToken) {
                        idToken = result.idToken
                        provider = Google
                    }*/
                }
            }
        },
        fallback = {
            //call gotrue.loginWith(Google) e.g. in your ViewModel
        }
    )
}