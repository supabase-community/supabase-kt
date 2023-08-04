package io.github.jan.supabase.compose.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption


internal fun getSignInRequest(config: GoogleLoginConfig?): BeginSignInRequest {
    val tokenIdRequestOptions = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
    config?.let { options ->
        tokenIdRequestOptions
            .setServerClientId(options.serverClientId)
            .setSupported(options.isSupported)
            .setFilterByAuthorizedAccounts(options.filterByAuthorizedAccounts)
            .setNonce(options.nonce)
    }
    config?.associateLinkedAccounts?.let {
        tokenIdRequestOptions.associateLinkedAccounts(it.first, it.second)
    }
    return BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(tokenIdRequestOptions.build()).build()
}

internal fun getGoogleIDOptions(config: GoogleLoginConfig?): GetGoogleIdOption {
    val googleIdOption = GetGoogleIdOption.Builder()
    config?.let { options ->
        googleIdOption.setServerClientId(options.serverClientId)
        googleIdOption.setFilterByAuthorizedAccounts(options.filterByAuthorizedAccounts)
        googleIdOption.setNonce(options.nonce)

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