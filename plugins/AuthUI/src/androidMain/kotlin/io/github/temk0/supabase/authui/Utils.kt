package io.github.temk0.supabase.authui

import com.google.android.gms.auth.api.identity.BeginSignInRequest


fun getSignInRequest(config: GoogleLoginConfig?): BeginSignInRequest {
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