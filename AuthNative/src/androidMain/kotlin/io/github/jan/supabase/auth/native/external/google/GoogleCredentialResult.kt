package io.github.jan.supabase.auth.native.external.google

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

internal sealed interface GoogleCredentialResult {

    data class Success(val credential: GoogleIdTokenCredential): GoogleCredentialResult

    data class Error(val message: String, val exception: Exception? = null): GoogleCredentialResult

}