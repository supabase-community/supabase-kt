package io.github.jan.supabase.auth

sealed interface OAuthServerApi {

    suspend fun getAuthorizationDetails(
        authorizationId: String,

    )

    suspend fun approveAuthorization(
        authorizationId: String,

    )

    suspend fun denyAuthorization(
        authorizationId: String,

    )

}