package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.generateRedirectUrl
import io.github.jan.supacompose.auth.user.UserSession
import io.ktor.client.call.body
import io.ktor.http.HttpMethod

interface AuthProvider<C, R> {

    suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    )

    suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    ): R

}

interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) {
        if(config == null) throw IllegalArgumentException("Credentials are required")
        val encodedCredentials = encodeCredentials(config)
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/token?grant_type=password", body = encodedCredentials)
        response.body<UserSession>().also {
            onSuccess(it)
        }
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R {
        if (config == null) throw IllegalArgumentException("Credentials are required")
        val body = encodeCredentials(config)
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/signup", body = body)
        return decodeResult(response.body())
    }

    fun decodeResult(body: String): R

    fun encodeCredentials(credentials: C.() -> Unit): String

}