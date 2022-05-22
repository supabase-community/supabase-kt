package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.exceptions.RestException
import io.ktor.client.call.body
import io.ktor.http.HttpMethod

interface AuthProvider<C, R> {

    suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        credentials: (C.() -> Unit)? = null
    )

    suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit = {},
        credentials: (C.() -> Unit)? = null
    ): R

}

interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
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
            val error = when(it) {
                is RestException -> AuthFail.InvalidCredentials
                else -> AuthFail.Error(it)
            }
            onFail(error)
        }
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        credentials: (C.() -> Unit)?
    ): R {
        return kotlin.runCatching {
            if(credentials == null) throw IllegalArgumentException("Credentials are required")
            val body = encodeCredentials(credentials)
            val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/signup", body = body)
            decodeResult(response.body())
        }.onFailure {
            val error = when(it) {
                is RestException -> AuthFail.InvalidCredentials
                else -> AuthFail.Error(it)
            }
            onFail(error)
        }.getOrThrow()
    }

    fun decodeResult(body: String): R

    fun encodeCredentials(credentials: C.() -> Unit): String

}