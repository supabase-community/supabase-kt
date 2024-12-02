package io.supabase.compose.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import io.supabase.compose.auth.GoogleLoginConfig

internal fun getSignInWithGoogleOptions(
    config: GoogleLoginConfig?,
    nonce: String? = null
): GetSignInWithGoogleOption {
    val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(config?.serverClientId ?: error("Trying to use Google Auth without setting the serverClientId"))
    signInWithGoogleOption.setNonce(nonce)
    return signInWithGoogleOption.build()
}

internal fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}