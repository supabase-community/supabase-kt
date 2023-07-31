package io.github.jan.supabase.compose.auth

import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInCredential


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

fun SignInCredential.fetchExtraData(config: GoogleLoginConfig):List<LoginConfig.ExtraData>{
    if(config.extraData.isNullOrEmpty()) return emptyList()

    config.extraData.forEach {
        when(it){
            is GoogleLoginConfig.DisplayName -> it.value = this.displayName
            is GoogleLoginConfig.FamilyName -> it.value = this.familyName
            is GoogleLoginConfig.GivenName -> it.value = this.givenName
            is GoogleLoginConfig.ID -> it.value = this.id
            is GoogleLoginConfig.ProfilePic -> it.value = this.profilePictureUri.toString()
        }
    }

    return config.extraData
}