package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession
import io.ktor.client.call.body
import io.ktor.http.HttpMethod

interface AuthProvider<C, R> {

    suspend fun login(supabaseClient: SupabaseClient, onSuccess: suspend (UserSession) -> Unit, onFail: (OAuthFail) -> Unit, credentials: (C.() -> Unit)? = null)

    suspend fun signUp(supabaseClient: SupabaseClient, credentials: C.() -> Unit): R

}

interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (OAuthFail) -> Unit,
        credentials: (C.() -> Unit)?
    ) {
        kotlin.runCatching {
            if(credentials == null) throw IllegalArgumentException("Credentials are required")
            val encodedCredentials = encodeCredentials(credentials)
            val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/token?grant_type=password", body = encodedCredentials)
            response.body<UserSession>()
        }.onSuccess {
            onSuccess(it)
        }.onFailure {
            onFail(OAuthFail.Error(it))
        }
    }

    override suspend fun signUp(supabaseClient: SupabaseClient, credentials: C.() -> Unit): R {
        val body = encodeCredentials(credentials)
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/signup", body = body)
        return decodeResult(response.body())
    }

    fun decodeResult(body: String): R

    fun encodeCredentials(credentials: C.() -> Unit): String

}