package io.github.jan.supabase.compose.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

//Inspired by https://github.com/stevdza-san/OneTapCompose

@Composable
actual fun NativeGoogleLogin(
    state: NativeSignInState,
    clientId: String,
    nonce: String?,
    onResult: (NativeSignInResult) -> Unit,
    fallback: (() -> Unit)?
) {
    val activity = LocalContext.current as Activity
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val oneTapClient = Identity.getSignInClient(activity)
                val credentials = oneTapClient.getSignInCredentialFromIntent(result.data)
                val tokenId = credentials.googleIdToken
                if (tokenId != null) {
                    onResult(NativeSignInResult.Success(tokenId))
                    state.reset()
                }
            } else {
                onResult(NativeSignInResult.ClosedByUser)
                state.reset()
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    onResult(NativeSignInResult.Canceled)
                    state.reset()
                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    onResult(NativeSignInResult.NetworkError)
                    state.reset()
                }
                else -> {
                    onResult(NativeSignInResult.Error(e.message ?: "Unknown Error"))
                    state.reset()
                }
            }
        }
    }

    LaunchedEffect(state.started) {
        if (state.started) {
            signIn(
                activity = activity,
                clientId = clientId,
                nonce = nonce,
                launchActivityResult = { intentSenderRequest ->
                    activityLauncher.launch(intentSenderRequest)
                },
                onResult = {
                    onResult(it)
                    state.reset()
                }
            )
        }
    }
}

private fun signIn(
    activity: Activity,
    clientId: String,
    nonce: String?,
    launchActivityResult: (IntentSenderRequest) -> Unit,
    onResult: (NativeSignInResult) -> Unit
) {
    val oneTapClient = Identity.getSignInClient(activity)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setNonce(nonce)
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(true)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    oneTapClient.beginSignIn(signInRequest)
        .addOnSuccessListener { result ->
            try {
                launchActivityResult(
                    IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender
                    ).build()
                )
            } catch (e: Exception) {
                Logger.e(e) { "Error launching activity result" }
                onResult(NativeSignInResult.Error(e.message ?: "Unknown Error"))
            }
        }
        .addOnFailureListener {
            signUp(
                activity = activity,
                clientId = clientId,
                nonce = nonce,
                launchActivityResult = launchActivityResult,
                onResult = onResult
            )
        }
}

private fun signUp(
    activity: Activity,
    clientId: String,
    nonce: String?,
    launchActivityResult: (IntentSenderRequest) -> Unit,
    onResult: (NativeSignInResult) -> Unit
) {
    val oneTapClient = Identity.getSignInClient(activity)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setNonce(nonce)
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .build()

    oneTapClient.beginSignIn(signInRequest)
        .addOnSuccessListener { result ->
            try {
                launchActivityResult(
                    IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender
                    ).build()
                )
            } catch (e: Exception) {
                Logger.e(e) { "Error launching activity result" }
                onResult(NativeSignInResult.Error(e.message ?: "Unknown Error"))
            }
        }
        .addOnFailureListener {
            onResult(NativeSignInResult.AccountNotFound)
        }
}