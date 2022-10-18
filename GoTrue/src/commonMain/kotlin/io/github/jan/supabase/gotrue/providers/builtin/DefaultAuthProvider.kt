package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.checkErrors
import io.github.jan.supabase.gotrue.generateRedirectUrl
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject

sealed interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) {
        if(config == null) throw IllegalArgumentException("Credentials are required")
        val encodedCredentials = encodeCredentials(config)
        val response = supabaseClient.httpClient.post(supabaseClient.gotrue.resolveUrl("token?grant_type=password")) {
            setBody(encodedCredentials)
        }
        response.checkErrors()
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
        val finalRedirectUrl = supabaseClient.gotrue.generateRedirectUrl(redirectUrl)
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        val body = encodeCredentials(config)
        val response = supabaseClient.httpClient.post(supabaseClient.gotrue.resolveUrl("signup$redirect")) {
            setBody(body)
        }
        response.checkErrors()
        val json = response.body<JsonObject>()
        return decodeResult(json)
    }

    fun decodeResult(json: JsonObject): R

    fun encodeCredentials(credentials: C.() -> Unit): String

}