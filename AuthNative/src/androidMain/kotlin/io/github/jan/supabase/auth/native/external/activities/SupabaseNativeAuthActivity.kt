package io.github.jan.supabase.auth.native.external.activities

import android.content.Intent
import android.os.Bundle
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.native.AuthFlowManager
import io.github.jan.supabase.auth.native.external.google.GoogleCredentialResult
import io.github.jan.supabase.auth.native.external.google.GoogleDialogType
import io.github.jan.supabase.auth.native.external.google.getGoogleBottomSheetOptions
import io.github.jan.supabase.auth.native.external.google.getGoogleButtonOptions
import io.github.jan.supabase.auth.native.hash
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class SupabaseNativeAuthActivity: SupabaseAuthActivity() {

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialManager = CredentialManager.create(this)
        handleIntent(intent ?: return)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let(::handleIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            AuthFlowManager.handleSignInResult(
                GoogleCredentialResult.Error("Sign in canceled or Activity destroyed")
            )
        }
    }

    private fun handleIntent(intent: Intent) {
        val googleClientId = intent.getStringExtra(EXTRA_CLIENT_ID) ?: return returnWithError("No client id provided")
        val hashedNonce = intent.getStringExtra(EXTRA_NONCE)?.hash() ?: return returnWithError("No nonce provided")
        val type = intent.getStringExtra(EXTRA_DIALOG_TYPE)?.let { GoogleDialogType.valueOf(it) } ?: return returnWithError("No type provided")
        lifecycleScope.launch {
            val result = try {
                val response = makeRequest(googleClientId, hashedNonce, type = type)
                if(response != null) parseCredential(response.credential) else GoogleCredentialResult.ClosedByUser
            } catch(e: Exception) {
                GoogleCredentialResult.Error(e.localizedMessage ?: "Error", e)
            }
            AuthFlowManager.handleSignInResult(result)
            returnToApp()
        }
    }

    private fun returnWithError(message: String) {
        AuthFlowManager.handleSignInResult(GoogleCredentialResult.Error(message))
        returnToApp()
    }


    private suspend fun tryRequest(
        clientId: String?,
        nonce: String?,
        filterByAuthorizedAccounts: Boolean = false,
        type: GoogleDialogType
    ): GetCredentialResponse {
        val option = when(type) {
            GoogleDialogType.BOTTOM_SHEET -> getGoogleBottomSheetOptions(clientId, filterByAuthorizedAccounts, nonce)
            GoogleDialogType.DIALOG -> getGoogleButtonOptions(clientId, nonce)
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        return credentialManager.getCredential(this, request)
    }

    private suspend fun makeRequest(
        clientId: String?,
        nonce: String?,
        type: GoogleDialogType
    ): GetCredentialResponse? {
        return try {
            tryRequest(clientId, nonce, true, type)
        } catch(e: GetCredentialException) {
            e.printStackTrace()
            if(type == GoogleDialogType.BOTTOM_SHEET) {
                tryRequest(clientId, nonce, false, type)
            } else null
        } catch(e: GetCredentialCancellationException) {
            e.printStackTrace()
            null
        }
    }
    private suspend fun parseCredential(
        credential: Credential,
    ): GoogleCredentialResult {
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        return GoogleCredentialResult.Success(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        return GoogleCredentialResult.Error(e.localizedMessage ?: "Google ID parsing exception", e)
                    } catch (e: Exception) {
                        currentCoroutineContext().ensureActive()
                        return GoogleCredentialResult.Error(e.localizedMessage ?: "Error", e)
                    }
                } else {
                    return GoogleCredentialResult.Error("Unexpected type of credential")
                }
            }

            else -> {
                return GoogleCredentialResult.Error("Unsupported credentials")
            }
        }
    }

    companion object {
        const val EXTRA_CLIENT_ID = "google_client_id"
        const val EXTRA_NONCE = "nonce"
        const val EXTRA_DIALOG_TYPE = "dialog_type"
    }

}