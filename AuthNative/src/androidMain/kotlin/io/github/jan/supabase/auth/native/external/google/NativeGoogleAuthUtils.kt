package io.github.jan.supabase.auth.native.external.google

import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

internal fun getGoogleButtonOptions(
    clientId: String?,
    nonce: String? = null
): GetSignInWithGoogleOption {
    val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId ?: error("Trying to use Google Auth without setting the serverClientId"))
    signInWithGoogleOption.setNonce(nonce)
    return signInWithGoogleOption.build()
}

internal fun getGoogleBottomSheetOptions(
    clientId: String?,
    filterByAuthorizedAccounts: Boolean = true,
    nonce: String? = null
): GetGoogleIdOption {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
        .setServerClientId(clientId ?: error("Trying to use Google Auth without setting the serverClientId"))
        .setNonce(nonce)
        .build()
    return googleIdOption
}