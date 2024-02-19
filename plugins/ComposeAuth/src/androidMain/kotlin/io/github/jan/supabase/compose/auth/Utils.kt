package io.github.jan.supabase.compose.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

internal fun getGoogleIDOptions(
    config: GoogleLoginConfig?,
    nonce: String? = null
): GetGoogleIdOption {
    val googleIdOption = GetGoogleIdOption.Builder()
    config?.let { options ->
        googleIdOption.setServerClientId(options.serverClientId)
        googleIdOption.setFilterByAuthorizedAccounts(options.filterByAuthorizedAccounts)
        googleIdOption.setNonce(nonce)

        options.associateLinkedAccounts?.let {
            googleIdOption.associateLinkedAccounts(it.first, it.second)
        }
    }
    return googleIdOption.build()
}

internal fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}