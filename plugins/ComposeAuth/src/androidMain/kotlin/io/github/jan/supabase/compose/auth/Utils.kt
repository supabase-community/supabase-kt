package io.github.jan.supabase.compose.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

internal fun getGoogleButtonOptions(
    config: GoogleLoginConfig?,
    nonce: String? = null
): GetSignInWithGoogleOption {
    val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(config?.serverClientId ?: error("Trying to use Google Auth without setting the serverClientId"))
    signInWithGoogleOption.setNonce(nonce)
    return signInWithGoogleOption.build()
}

internal fun getGoogleBottomSheetOptions(
    config: GoogleLoginConfig?,
    filterByAuthorizedAccounts: Boolean = true,
    nonce: String? = null
): GetGoogleIdOption {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
        .setServerClientId(config?.serverClientId ?: error("Trying to use Google Auth without setting the serverClientId"))
        .setNonce(nonce)
    .build()
    return googleIdOption
}

internal fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}